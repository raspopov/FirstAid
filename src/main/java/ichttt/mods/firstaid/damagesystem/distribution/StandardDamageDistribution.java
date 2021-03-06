package ichttt.mods.firstaid.damagesystem.distribution;

import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.DamageSource;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StandardDamageDistribution extends DamageDistribution {
    private final List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> partList;

    public StandardDamageDistribution(List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> partList) {
        this.partList = partList;
    }

    @Override
    @Nonnull
    protected List<Pair<EntityEquipmentSlot, EnumPlayerPart[]>> getPartList() {
        return partList;
    }

    @Override
    public float distributeDamage(float damage, @Nonnull EntityPlayer player, @Nonnull DamageSource source, boolean addStat) {
        float rest = super.distributeDamage(damage, player, source, addStat);
        if (rest > 0) {
            EnumPlayerPart[] parts = partList.get(partList.size() - 1).getRight();
            Optional<EnumPlayerPart> playerPart = Arrays.stream(parts).filter(enumPlayerPart -> !enumPlayerPart.getNeighbours().isEmpty()).findAny();
            if (playerPart.isPresent()) {
                List<EnumPlayerPart> neighbours = playerPart.get().getNeighbours();
                neighbours = neighbours.stream().filter(part -> partList.stream().noneMatch(pair -> Arrays.stream(pair.getRight()).anyMatch(p2 -> p2 == part))).collect(Collectors.toList());
                for (EnumPlayerPart part : neighbours)
                    rest = new PreferredDamageDistribution(part).distributeDamage(rest, player, source, addStat);
            }
        }
        return rest;
    }
}
