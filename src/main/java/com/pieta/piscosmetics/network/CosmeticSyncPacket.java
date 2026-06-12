package com.pieta.piscosmetics.network;

import java.util.*;

import com.pieta.piscosmetics.PisCosmetics;
import com.pieta.piscosmetics.api.CosmeticDefinition;
import com.pieta.piscosmetics.api.ParticleEmitter;
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
            buf.writeUtf(def.attach().orElse(""));
            List<String> hideArmor = def.hideArmor();
            buf.writeInt(hideArmor.size());
            for (String s : hideArmor) {
                buf.writeUtf(s);
            }

            buf.writeFloat(def.translateX().orElse(0f));
            buf.writeFloat(def.translateY().orElse(0f));
            buf.writeFloat(def.translateZ().orElse(0f));
            buf.writeFloat(def.rotateX().orElse(0f));
            buf.writeFloat(def.rotateY().orElse(0f));
            buf.writeFloat(def.rotateZ().orElse(0f));
            buf.writeFloat(def.scale().orElse(1f));

            var emitters = def.particleEmitters();
            buf.writeInt(emitters.size());
            for (ParticleEmitter emitter : emitters) {
                buf.writeUtf(emitter.trigger());
                buf.writeResourceLocation(emitter.particleId());
                buf.writeInt(emitter.rate());
                buf.writeDouble(emitter.spread());
                buf.writeInt(emitter.count());
                buf.writeBoolean(emitter.offsetX().isPresent());
                emitter.offsetX().ifPresent(buf::writeDouble);
                buf.writeBoolean(emitter.offsetY().isPresent());
                emitter.offsetY().ifPresent(buf::writeDouble);
                buf.writeBoolean(emitter.offsetZ().isPresent());
                emitter.offsetZ().ifPresent(buf::writeDouble);
                buf.writeUtf(emitter.color().orElse(""));
                buf.writeBoolean(emitter.texture().isPresent());
                emitter.texture().ifPresent(buf::writeResourceLocation);
                buf.writeFloat(emitter.customSize().orElse(0.25f));
                buf.writeInt(emitter.lifetime().orElse(30));
            }

            buf.writeBoolean(def.icon().isPresent());
            def.icon().ifPresent(buf::writeResourceLocation);
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

            String attachStr = buf.readUtf();
            Optional<String> attach = attachStr.isEmpty() ? Optional.empty() : Optional.of(attachStr);

            int hideArmorCount = buf.readInt();
            List<String> hideArmor = new ArrayList<>();
            for (int h = 0; h < hideArmorCount; h++) {
                hideArmor.add(buf.readUtf());
            }

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

            int emitterCount = buf.readInt();
            java.util.List<ParticleEmitter> emitters = new java.util.ArrayList<>();
            for (int j = 0; j < emitterCount; j++) {
                String trigger = buf.readUtf();
                ResourceLocation particleId = buf.readResourceLocation();
                int rate = buf.readInt();
                double spread = buf.readDouble();
                int count = buf.readInt();

                Optional<Double> offsetX = buf.readBoolean() ? Optional.of(buf.readDouble()) : Optional.empty();
                Optional<Double> offsetY = buf.readBoolean() ? Optional.of(buf.readDouble()) : Optional.empty();
                Optional<Double> offsetZ = buf.readBoolean() ? Optional.of(buf.readDouble()) : Optional.empty();

                String colorStr = buf.readUtf();
                Optional<String> color = colorStr.isEmpty() ? Optional.empty() : Optional.of(colorStr);

                Optional<ResourceLocation> textureOpt = buf.readBoolean() ? Optional.of(buf.readResourceLocation()) : Optional.empty();
                float customSize = buf.readFloat();
                Optional<Float> customSizeOpt = customSize == 0.25f ? Optional.empty() : Optional.of(customSize);
                int lifetime = buf.readInt();
                Optional<Integer> lifetimeOpt = lifetime == 30 ? Optional.empty() : Optional.of(lifetime);

                emitters.add(new ParticleEmitter(
                        trigger, particleId, rate, spread, count,
                        offsetX, offsetY, offsetZ, color, textureOpt, customSizeOpt, lifetimeOpt
                ));
            }

            Optional<ResourceLocation> icon = buf.readBoolean()
                    ? Optional.of(buf.readResourceLocation())
                    : Optional.empty();

            defs.put(id, new CosmeticDefinition(
                    slot,
                    model,
                    texture,
                    animation,
                    name,
                    attach,
                    translateX,
                    translateY,
                    translateZ,
                    rotateX,
                    rotateY,
                    rotateZ,
                    scaleOpt,
                    emitters,
                    hideArmor,
                    icon
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