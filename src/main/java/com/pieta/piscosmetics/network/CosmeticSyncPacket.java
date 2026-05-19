package com.pieta.piscosmetics.network;

import java.util.Optional;
import com.pieta.piscosmetics.PisCosmetics;
import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.pieta.piscosmetics.api.CosmeticDefinition.ParticleSettings;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.HashMap;
import java.util.Map;

public record CosmeticSyncPacket(Map<ResourceLocation, CosmeticDefinition> definitions) implements CustomPacketPayload {

    public static final Type<CosmeticSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PisCosmetics.MODID, "cosmetic_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CosmeticSyncPacket> STREAM_CODEC = StreamCodec.of(
            CosmeticSyncPacket::encode,
            CosmeticSyncPacket::decode
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void encode(RegistryFriendlyByteBuf buf, CosmeticSyncPacket packet) {
        buf.writeInt(packet.definitions.size());
        for (var entry : packet.definitions.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            // Use codec to write each definition
            buf.writeUtf(entry.getValue().slot());
            entry.getValue().model().ifPresentOrElse(
                    loc -> { buf.writeBoolean(true); buf.writeResourceLocation(loc); },
                    () -> buf.writeBoolean(false)
            );
            entry.getValue().texture().ifPresentOrElse(
                    loc -> { buf.writeBoolean(true); buf.writeResourceLocation(loc); },
                    () -> buf.writeBoolean(false)
            );
            buf.writeUtf(entry.getValue().animation().orElse(""));
            buf.writeUtf(entry.getValue().name().orElse(""));
            buf.writeFloat(entry.getValue().translateX().orElse(0f));
            buf.writeFloat(entry.getValue().translateY().orElse(0f));
            buf.writeFloat(entry.getValue().translateZ().orElse(0f));
            buf.writeFloat(entry.getValue().rotateX().orElse(0f));
            buf.writeFloat(entry.getValue().rotateY().orElse(0f));
            buf.writeFloat(entry.getValue().rotateZ().orElse(0f));
            buf.writeFloat(entry.getValue().scale().orElse(1f));

            var particles = entry.getValue().particles();
            buf.writeBoolean(particles.isPresent());
            if (particles.isPresent()) {
                var p = particles.get();
                buf.writeResourceLocation(p.particleId());
                buf.writeInt(p.rate());
                buf.writeDouble(p.spread());
                buf.writeFloat(p.offsetX().orElse(0f));
                buf.writeFloat(p.offsetY().orElse(0f));
                buf.writeFloat(p.offsetZ().orElse(0f));
            }
        }
    }

    private static CosmeticSyncPacket decode(RegistryFriendlyByteBuf buf) {
        Map<ResourceLocation, CosmeticDefinition> defs = new HashMap<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            ResourceLocation id = buf.readResourceLocation();
            String slot = buf.readUtf();
            Optional<ResourceLocation> model = buf.readBoolean() ?
                    Optional.of(buf.readResourceLocation()) : Optional.empty();
            Optional<ResourceLocation> texture = buf.readBoolean() ?
                    Optional.of(buf.readResourceLocation()) : Optional.empty();
            String anim = buf.readUtf();
            String name = buf.readUtf();
            float tx = buf.readFloat();
            float ty = buf.readFloat();
            float tz = buf.readFloat();
            float rx = buf.readFloat();
            float ry = buf.readFloat();
            float rz = buf.readFloat();
            float scale = buf.readFloat();

            defs.put(id, new CosmeticDefinition(
                    slot,
                    model,
                    texture,
                    anim.isEmpty() ? Optional.empty() : Optional.of(anim),
                    name.isEmpty() ? Optional.empty() : Optional.of(name),
                    tx == 0 ? Optional.empty() : Optional.of(tx),
                    ty == 0 ? Optional.empty() : Optional.of(ty),
                    tz == 0 ? Optional.empty() : Optional.of(tz),
                    rx == 0 ? Optional.empty() : Optional.of(rx),
                    ry == 0 ? Optional.empty() : Optional.of(ry),
                    rz == 0 ? Optional.empty() : Optional.of(rz),
                    scale == 1 ? Optional.empty() : Optional.of(scale),
                    buf.readBoolean() ? Optional.of(new ParticleSettings(
                            buf.readResourceLocation(),
                            buf.readInt(),
                            buf.readDouble(),
                            Optional.of(buf.readFloat()).filter(f -> f != 0),
                            Optional.of(buf.readFloat()).filter(f -> f != 0),
                            Optional.of(buf.readFloat()).filter(f -> f != 0)
                    )) : Optional.empty()
            ));
        }
        return new CosmeticSyncPacket(defs);
    }

    public static void handle(CosmeticSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            CosmeticDataLoader.DEFINITIONS.clear();
            CosmeticDataLoader.DEFINITIONS.putAll(packet.definitions);
        });
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(TYPE, STREAM_CODEC, CosmeticSyncPacket::handle);
    }
}