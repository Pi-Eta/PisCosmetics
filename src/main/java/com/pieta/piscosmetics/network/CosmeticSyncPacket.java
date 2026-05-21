package com.pieta.piscosmetics.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.pieta.piscosmetics.PisCosmetics;
import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.api.CosmeticDefinition.ParticleSettings;
import com.pieta.piscosmetics.data.CosmeticDataLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

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
        buf.writeInt(packet.definitions().size());
        for (var entry : packet.definitions().entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            CosmeticDefinition def = entry.getValue();

            buf.writeUtf(def.slot());

            def.model().ifPresentOrElse(
                    loc -> { buf.writeBoolean(true); buf.writeResourceLocation(loc); },
                    () -> buf.writeBoolean(false)
            );

            def.texture().ifPresentOrElse(
                    loc -> { buf.writeBoolean(true); buf.writeResourceLocation(loc); },
                    () -> buf.writeBoolean(false)
            );

            buf.writeUtf(def.animation().orElse(""));
            buf.writeUtf(def.name().orElse(""));
            buf.writeBoolean(def.elytra().orElse(false));  // elytra AFTER name

            buf.writeFloat(def.translateX().orElse(0f));
            buf.writeFloat(def.translateY().orElse(0f));
            buf.writeFloat(def.translateZ().orElse(0f));
            buf.writeFloat(def.rotateX().orElse(0f));
            buf.writeFloat(def.rotateY().orElse(0f));
            buf.writeFloat(def.rotateZ().orElse(0f));
            buf.writeFloat(def.scale().orElse(1f));

            var particles = def.particles();
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

            Optional<ResourceLocation> model = buf.readBoolean()
                    ? Optional.of(buf.readResourceLocation())
                    : Optional.empty();

            Optional<ResourceLocation> texture = buf.readBoolean()
                    ? Optional.of(buf.readResourceLocation())
                    : Optional.empty();

            String anim = buf.readUtf();
            Optional<String> animation = anim.isEmpty() ? Optional.empty() : Optional.of(anim);

            String nameStr = buf.readUtf();
            Optional<String> name = nameStr.isEmpty() ? Optional.empty() : Optional.of(nameStr);

            boolean elytraRaw = buf.readBoolean();
            Optional<Boolean> elytra = Optional.of(elytraRaw);

            float tx = buf.readFloat();
            float ty = buf.readFloat();
            float tz = buf.readFloat();
            float rx = buf.readFloat();
            float ry = buf.readFloat();
            float rz = buf.readFloat();
            float scale = buf.readFloat();

            Optional<Float> translateX = tx == 0 ? Optional.empty() : Optional.of(tx);
            Optional<Float> translateY = ty == 0 ? Optional.empty() : Optional.of(ty);
            Optional<Float> translateZ = tz == 0 ? Optional.empty() : Optional.of(tz);
            Optional<Float> rotateX = rx == 0 ? Optional.empty() : Optional.of(rx);
            Optional<Float> rotateY = ry == 0 ? Optional.empty() : Optional.of(ry);
            Optional<Float> rotateZ = rz == 0 ? Optional.empty() : Optional.of(rz);
            Optional<Float> scaleOpt = scale == 1f ? Optional.empty() : Optional.of(scale);

            Optional<ParticleSettings> particles = Optional.empty();
            if (buf.readBoolean()) {
                ResourceLocation particleId = buf.readResourceLocation();
                int rate = buf.readInt();
                double spread = buf.readDouble();
                float ox = buf.readFloat();
                float oy = buf.readFloat();
                float oz = buf.readFloat();
                particles = Optional.of(new ParticleSettings(
                        particleId,
                        rate,
                        spread,
                        ox == 0 ? Optional.empty() : Optional.of(ox),
                        oy == 0 ? Optional.empty() : Optional.of(oy),
                        oz == 0 ? Optional.empty() : Optional.of(oz)
                ));
            }

            defs.put(id, new CosmeticDefinition(
                    slot,
                    model,
                    texture,
                    animation,
                    name,
                    translateX,
                    translateY,
                    translateZ,
                    rotateX,
                    rotateY,
                    rotateZ,
                    scaleOpt,
                    particles,
                    elytra
            ));
        }
        return new CosmeticSyncPacket(defs);
    }

    public static void handle(CosmeticSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            CosmeticDataLoader.DEFINITIONS.clear();
            CosmeticDataLoader.DEFINITIONS.putAll(packet.definitions());
        });
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(TYPE, STREAM_CODEC, CosmeticSyncPacket::handle);
    }
}