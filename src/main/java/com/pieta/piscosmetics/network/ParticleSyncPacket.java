package com.pieta.piscosmetics.network;

import com.pieta.piscosmetics.PisCosmetics;
import com.pieta.piscosmetics.api.ParticleEmitter;
import com.pieta.piscosmetics.client.ClientParticleData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ParticleSyncPacket(int entityId, List<ParticleEmitter> emitters) implements CustomPacketPayload {

    public static final Type<ParticleSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PisCosmetics.MODID, "particle_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleSyncPacket> STREAM_CODEC = StreamCodec.of(
            ParticleSyncPacket::encode,
            ParticleSyncPacket::decode
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void encode(RegistryFriendlyByteBuf buf, ParticleSyncPacket packet) {
        buf.writeInt(packet.entityId);
        buf.writeInt(packet.emitters.size());
        for (ParticleEmitter emitter : packet.emitters) {
            buf.writeUtf(emitter.trigger());
            buf.writeResourceLocation(emitter.particleId());
            buf.writeInt(emitter.rate());
            buf.writeDouble(emitter.spread());
            buf.writeInt(emitter.count());

            writeOptionalDouble(buf, emitter.offsetX());
            writeOptionalDouble(buf, emitter.offsetY());
            writeOptionalDouble(buf, emitter.offsetZ());

            buf.writeUtf(emitter.color().orElse(""));

            writeOptionalResourceLocation(buf, emitter.texture());
            buf.writeFloat(emitter.customSize().orElse(0.25f));
            buf.writeInt(emitter.lifetime().orElse(30));
        }
    }

    private static void writeOptionalDouble(RegistryFriendlyByteBuf buf, Optional<Double> opt) {
        buf.writeBoolean(opt.isPresent());
        opt.ifPresent(buf::writeDouble);
    }

    private static void writeOptionalResourceLocation(RegistryFriendlyByteBuf buf, Optional<ResourceLocation> opt) {
        buf.writeBoolean(opt.isPresent());
        opt.ifPresent(buf::writeResourceLocation);
    }

    private static ParticleSyncPacket decode(RegistryFriendlyByteBuf buf) {
        int entityId = buf.readInt();
        int size = buf.readInt();
        List<ParticleEmitter> emitters = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            String trigger = buf.readUtf();
            ResourceLocation particleId = buf.readResourceLocation();
            int rate = buf.readInt();
            double spread = buf.readDouble();
            int count = buf.readInt();

            Optional<Double> offsetX = readOptionalDouble(buf);
            Optional<Double> offsetY = readOptionalDouble(buf);
            Optional<Double> offsetZ = readOptionalDouble(buf);

            String colorStr = buf.readUtf();
            Optional<String> color = colorStr.isEmpty() ? Optional.empty() : Optional.of(colorStr);

            Optional<ResourceLocation> texture = readOptionalResourceLocation(buf);
            float customSize = buf.readFloat();
            Optional<Float> customSizeOpt = customSize == 0.25f ? Optional.empty() : Optional.of(customSize);
            int lifetime = buf.readInt();
            Optional<Integer> lifetimeOpt = lifetime == 30 ? Optional.empty() : Optional.of(lifetime);

            emitters.add(new ParticleEmitter(
                    trigger, particleId, rate, spread, count,
                    offsetX, offsetY, offsetZ, color, texture, customSizeOpt, lifetimeOpt
            ));
        }

        return new ParticleSyncPacket(entityId, emitters);
    }

    private static Optional<Double> readOptionalDouble(RegistryFriendlyByteBuf buf) {
        return buf.readBoolean() ? Optional.of(buf.readDouble()) : Optional.empty();
    }

    private static Optional<ResourceLocation> readOptionalResourceLocation(RegistryFriendlyByteBuf buf) {
        return buf.readBoolean() ? Optional.of(buf.readResourceLocation()) : Optional.empty();
    }

    public static void handle(ParticleSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientParticleData.setParticleEmitters(packet.entityId, packet.emitters);
        });
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(TYPE, STREAM_CODEC, ParticleSyncPacket::handle);
    }
}