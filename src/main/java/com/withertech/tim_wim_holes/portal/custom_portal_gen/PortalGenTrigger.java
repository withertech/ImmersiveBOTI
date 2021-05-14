package com.withertech.tim_wim_holes.portal.custom_portal_gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;

import java.util.function.Function;

public abstract class PortalGenTrigger
{
	public static final Codec<PortalGenTrigger> triggerCodec;

	public static final Registry<Codec<? extends PortalGenTrigger>> codecRegistry;
	public static final Codec<UseItemTrigger> useItemTriggerCodec = RecordCodecBuilder.create(instance ->
	{
		return instance.group(
				Registry.ITEM.fieldOf("item").forGetter(o -> o.item),
				Codec.BOOL.optionalFieldOf("consume", false).forGetter(o -> o.consume)
		).apply(instance, instance.stable(UseItemTrigger::new));
	});
	public static final Codec<ThrowItemTrigger> throwItemTriggerCodec = RecordCodecBuilder.create(instance ->
	{
		return instance.group(
				Registry.ITEM.fieldOf("item").forGetter(o -> o.item)
		).apply(instance, instance.stable(ThrowItemTrigger::new));
	});
	public static final Codec<ConventionalDimensionChangeTrigger> conventionalDimensionChangeCodec =
			Codec.unit(ConventionalDimensionChangeTrigger::new);

	static
	{
		codecRegistry = new SimpleRegistry<>(
				RegistryKey.getOrCreateRootKey(new ResourceLocation("imm_ptl:custom_portal_gen_trigger")),
				Lifecycle.stable()
		);

		Registry.register(
				codecRegistry, new ResourceLocation("imm_ptl:use_item"), useItemTriggerCodec
		);
		Registry.register(
				codecRegistry, new ResourceLocation("imm_ptl:throw_item"), throwItemTriggerCodec
		);
		Registry.register(
				codecRegistry, new ResourceLocation("imm_ptl:conventional_dimension_change"),
				ConventionalDimensionChangeTrigger.conventionalDimensionChangeCodec
		);

		triggerCodec = codecRegistry.dispatchStable(
				PortalGenTrigger::getCodec,
				Function.identity()
		);
	}

	public abstract Codec<? extends PortalGenTrigger> getCodec();

	public static class UseItemTrigger extends PortalGenTrigger
	{
		public final Item item;
		public final boolean consume;

		public UseItemTrigger(Item item, boolean consume)
		{
			this.item = item;
			this.consume = consume;
		}

		@Override
		public Codec<? extends PortalGenTrigger> getCodec()
		{
			return useItemTriggerCodec;
		}
	}

	public static class ThrowItemTrigger extends PortalGenTrigger
	{
		public final Item item;

		public ThrowItemTrigger(Item item)
		{
			this.item = item;
		}

		@Override
		public Codec<? extends PortalGenTrigger> getCodec()
		{
			return throwItemTriggerCodec;
		}
	}

	public static class ConventionalDimensionChangeTrigger extends PortalGenTrigger
	{

		public ConventionalDimensionChangeTrigger()
		{
		}

		@Override
		public Codec<? extends PortalGenTrigger> getCodec()
		{
			return conventionalDimensionChangeCodec;
		}
	}


}
