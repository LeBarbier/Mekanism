package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.slot.InsertableSlot;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityModificationStation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ModificationStationContainer extends MekanismTileContainer<TileEntityModificationStation> {

    public ModificationStationContainer(int id, PlayerInventory inv, TileEntityModificationStation tile) {
        super(MekanismContainerTypes.MODIFICATION_STATION, id, inv, tile);
    }

    public ModificationStationContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityModificationStation.class));
    }

    @Override
    protected void addInventorySlots(@Nonnull PlayerInventory inv) {
        super.addInventorySlots(inv);

        for(int index = 0; index < inv.armorInventory.size(); index++) {
            addSlot(new InsertableSlot(inv, 36 + inv.armorInventory.size() - index - 1, 8, 8 + index * 18));
        }
        addSlot(new InsertableSlot(inv, inv.currentItem, 8, 12 + 18 * 4));
        addSlot(new InsertableSlot(inv, 40, 8, 14 + 18 * 5));
    }
}