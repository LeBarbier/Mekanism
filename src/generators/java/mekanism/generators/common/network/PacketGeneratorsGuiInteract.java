package mekanism.generators.common.network;

import java.util.function.Supplier;
import mekanism.api.functions.TriConsumer;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter.FissionReactorLogic;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter.FusionReactorLogic;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

/**
 * Used for informing the server that an action happened in a GUI
 */
public class PacketGeneratorsGuiInteract {

    private GeneratorsGuiInteraction interaction;
    private BlockPos tilePosition;
    private int extra;

    public PacketGeneratorsGuiInteract(GeneratorsGuiInteraction interaction, TileEntity tile) {
        this(interaction, tile.getPos());
    }

    public PacketGeneratorsGuiInteract(GeneratorsGuiInteraction interaction, TileEntity tile, int extra) {
        this(interaction, tile.getPos(), extra);
    }

    public PacketGeneratorsGuiInteract(GeneratorsGuiInteraction interaction, BlockPos tilePosition) {
        this(interaction, tilePosition, 0);
    }

    public PacketGeneratorsGuiInteract(GeneratorsGuiInteraction interaction, BlockPos tilePosition, int extra) {
        this.interaction = interaction;
        this.tilePosition = tilePosition;
        this.extra = extra;
    }

    public static void handle(PacketGeneratorsGuiInteract message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, player.world, message.tilePosition);
            if (tile != null) {
                message.interaction.consume(tile, player, message.extra);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketGeneratorsGuiInteract pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.interaction);
        buf.writeBlockPos(pkt.tilePosition);
        buf.writeVarInt(pkt.extra);
    }

    public static PacketGeneratorsGuiInteract decode(PacketBuffer buf) {
        return new PacketGeneratorsGuiInteract(buf.readEnumValue(GeneratorsGuiInteraction.class), buf.readBlockPos(), buf.readVarInt());
    }

    public enum GeneratorsGuiInteraction {
        INJECTION_RATE((tile, player, extra) -> {
            if (tile instanceof TileEntityFusionReactorController) {
                ((TileEntityFusionReactorController) tile).setInjectionRateFromPacket(extra);
            } else if (tile instanceof TileEntityFissionReactorCasing) {
                ((TileEntityFissionReactorCasing) tile).setRateLimitFromPacket(extra);
            }
        }),
        FISSION_LOGIC_TYPE((tile, player, extra) -> {
            if (tile instanceof TileEntityFissionReactorLogicAdapter) {
                ((TileEntityFissionReactorLogicAdapter) tile).setLogicTypeFromPacket(FissionReactorLogic.byIndexStatic(extra));
            }
        }),
        FUSION_LOGIC_TYPE((tile, player, extra) -> {
            if (tile instanceof TileEntityFusionReactorLogicAdapter) {
                ((TileEntityFusionReactorLogicAdapter) tile).setLogicTypeFromPacket(FusionReactorLogic.byIndexStatic(extra));
            }
        }),
        FISSION_ACTIVE((tile, player, extra) -> {
            if (tile instanceof TileEntityFissionReactorCasing) {
                ((TileEntityFissionReactorCasing) tile).setReactorActive(extra == 1);
            }
        });

        private TriConsumer<TileEntityMekanism, PlayerEntity, Integer> consumerForTile;

        GeneratorsGuiInteraction(TriConsumer<TileEntityMekanism, PlayerEntity, Integer> consumerForTile) {
            this.consumerForTile = consumerForTile;
        }

        public void consume(TileEntityMekanism tile, PlayerEntity player, int extra) {
            consumerForTile.accept(tile, player, extra);
        }
    }
}