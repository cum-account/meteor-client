package minegame159.meteorclient.modules.combat;

//Updated by squidoodly 24/04/2020

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.accountsfriends.FriendManager;
import minegame159.meteorclient.events.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.ToggleModule;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.utils.DamageCalcUtils;
import minegame159.meteorclient.utils.InvUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EnderCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;

import java.util.Iterator;

public class AutoTotem extends ToggleModule {
    private int totemCount;
    private String totemCountString = "0";

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public AutoTotem() {
        super(Category.Combat, "auto-totem", "Automatically equips totems.");
    }

    public final Setting<Boolean> smart = addSetting(new BoolSetting.Builder()
            .name("smart")
            .description("Only switches to totem when in danger of dying")
            .defaultValue(false)
            .build());


    public final Setting<Boolean> antiOneTap = addSetting(new BoolSetting.Builder()
            .name("anti-one-tap")
            .description("Tries to stop you dying with totems")
            .defaultValue(false)
            .build());

    @EventHandler
    private final Listener<TickEvent> onTick = new Listener<>(event -> {
        if (mc.currentScreen instanceof ContainerScreen<?>) return;

        int preTotemCount = totemCount;
        InvUtils.FindItemResult result = InvUtils.findItemWithCount(Items.TOTEM_OF_UNDYING);

        if (result.found() && !(mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) && !smart.get() && !antiOneTap.get()) {
            InvUtils.clickSlot(InvUtils.invIndexToSlotId(result.slot), 0, SlotActionType.PICKUP);
            InvUtils.clickSlot(InvUtils.OFFHAND_SLOT, 0, SlotActionType.PICKUP);
            InvUtils.clickSlot(InvUtils.invIndexToSlotId(result.slot), 0, SlotActionType.PICKUP);
        }else if(result.found() && !(mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) && smart.get() && (mc.player.getHealth() + mc.player.getAbsorptionAmount() < 10 || (mc.player.getHealth() + mc.player.getAbsorptionAmount()) - getHealthReduction() < 10)){
            InvUtils.clickSlot(InvUtils.invIndexToSlotId(result.slot), 0, SlotActionType.PICKUP);
            InvUtils.clickSlot(InvUtils.OFFHAND_SLOT, 0, SlotActionType.PICKUP);
            InvUtils.clickSlot(InvUtils.invIndexToSlotId(result.slot), 0, SlotActionType.PICKUP);
        }else if(result.found() && antiOneTap.get()){
            InvUtils.clickSlot(InvUtils.invIndexToSlotId(result.slot), 0, SlotActionType.PICKUP);
        }

        if (result.count != preTotemCount) totemCountString = Integer.toString(result.count);
    });

    @Override
    public String getInfoString() {
        return totemCountString;
    }

    private double getHealthReduction(){
        Iterator<Entity> entities =  mc.world.getEntities().iterator();
        double damageTaken = 0;
        for(int i = 0; entities.hasNext(); i++){
            Entity entity = entities.next();
            if(entity instanceof EnderCrystalEntity){
                damageTaken = DamageCalcUtils.resistanceReduction(mc.player, DamageCalcUtils.blastProtReduction(mc.player, DamageCalcUtils.armourCalc(mc.player, DamageCalcUtils.getDamageMultiplied(DamageCalcUtils.crystalDamage(mc.player, entity)))));
            }else if(entity instanceof PlayerEntity){
                if(!FriendManager.INSTANCE.contains((PlayerEntity) entity) && mc.player.getPos().distanceTo(entity.getPos()) < 5){
                    if(((PlayerEntity) entity).getActiveItem().getItem() instanceof SwordItem){
                        damageTaken = DamageCalcUtils.resistanceReduction(mc.player, DamageCalcUtils.normalProtReduction(mc.player, DamageCalcUtils.armourCalc(mc.player, DamageCalcUtils.getSwordDamage((PlayerEntity) entity))));
                    }
                }
            }
        }
        return damageTaken;
    }

    public void setSmart(boolean b){
        smart.set(b);
    }

    public void setAntiOneTap(boolean b){
        if(antiOneTap.get()){
            InvUtils.clickSlot(mc.player.inventory.getEmptySlot(), 0, SlotActionType.PICKUP);
        }
        antiOneTap.set(b);
    }
}
