package ichttt.mods.firstaid.damagesystem.debuff;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.network.MessagePlayHurtSound;
import ichttt.mods.firstaid.sound.EnumHurtSound;
import it.unimi.dsi.fastutil.floats.Float2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.floats.Float2IntMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;

import javax.annotation.Nonnull;

public class OnHitDebuff extends AbstractDebuff {
    private final Float2IntLinkedOpenHashMap map = new Float2IntLinkedOpenHashMap();
    private final EnumHurtSound sound;

    public OnHitDebuff(String potionName, @Nonnull EnumHurtSound sound) {
        super(potionName);
        this.sound = sound;
    }

    public OnHitDebuff addCondition(float damageNeeded, int timeInTicks) {
        map.put(damageNeeded, timeInTicks);
        return this;
    }

    @Override
    public void handleDamageTaken(float damage, float healthPerMax, EntityPlayerMP player) {
        if (!FirstAidConfig.enableDebuffs)
            return;
        int value = -1;
        for (Float2IntMap.Entry entry : map.float2IntEntrySet()) {
            if (damage >= entry.getFloatKey()) {
                value = Math.max(value, entry.getIntValue());
                player.addPotionEffect(new PotionEffect(effect, entry.getIntValue(), 0, false, false));
            }
        }
        if (value != -1 && sound != EnumHurtSound.NONE)
            FirstAid.NETWORKING.sendTo(new MessagePlayHurtSound(sound, value), player);
    }

    @Override
    public void handleHealing(float healingDone, float healthPerMax, EntityPlayerMP player) {

    }
}
