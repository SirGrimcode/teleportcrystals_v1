# Teleport Crystals

Ported from Minecraft 1.21.1 (Yarn mappings) to **Minecraft 26.3** (Fabric,
Mojang's official mappings - Yarn is no longer used as of 26.1).

- **Teleport Stone** - amethyst shards, an iron ingot, ender pearls.
  Sneak + use to bind your current position, use again to teleport there.
  Consumed on use.
- **Teleport Wand** - amethyst shards, a netherite ingot, blaze rods,
  ender pearls. Same behavior, but has durability instead, and can take
  Unbreaking / Mending.

Each item shows one of 4 baked textures depending on what's bound: clear
when unbound, green (Overworld), red (Nether), purple (End).

## Building

Push this to GitHub (see the earlier walkthrough) - `.github/workflows/build.yml`
now uses Java 25 and Gradle 8.12. Or locally: install Java 25 and Gradle
8.12+, then run `gradle build` from this folder (no wrapper is bundled).

**Before building**, double check `gradle.properties` and `build.gradle`
against <https://fabricmc.net/develop/> for 26.3 - the exact Loom plugin
version, Fabric Loader version, and Fabric API version are all moving
targets right now and the ones here are a best-effort snapshot.

## What changed from the 1.21.1 version

26.1 was a landmark Minecraft release: the game became fully unobfuscated
and Fabric dropped Yarn mappings for Mojang's official ones, Java jumped
to 25, and there were sweeping internal changes (rendering, recipes,
item/component internals, NBT API). This is a from-scratch rewrite against
official mappings, not a patch. Notable changes:

- Every vanilla class name changed: `World`->`Level`, `PlayerEntity`->`Player`,
  `ServerPlayerEntity`->`ServerPlayer`, `ItemStack` (same), `Item.Settings`->`Item.Properties`,
  `.maxCount()`->`.stacksTo()`, `.maxDamage()`->`.durability()`,
  `NbtCompound`->`CompoundTag`, `RegistryKey`->`ResourceKey`,
  `Text`->`Component`, `Hand`->`InteractionHand`, `TypedActionResult`->`InteractionResultHolder`,
  `SoundCategory`->`SoundSource`, `DataComponentTypes`->`DataComponents`,
  and (surprisingly) Yarn's `Identifier` name won out over the old
  `ResourceLocation` when Mojang renamed it at 1.21.11.
- `stack.damage(...)` -> `stack.hurtAndBreak(int, ServerLevel, ServerPlayer, Consumer<Item>)`.
- Since 1.21.5, `CompoundTag` getters return `Optional<T>` unless you pass
  a fallback value (`nbt.getInt("key", 0)`), in which case you get the
  plain type back - used throughout `TeleportData`.
- `CustomModelDataComponent` (a single float) became `CustomModelData`
  (four separate lists: floats/flags/strings/colors). The item texture
  switching now uses the **strings** list plus the newer
  `minecraft:select` item-model system (`assets/.../items/*.json`),
  replacing the old float-threshold `overrides` list entirely.
- Recipe JSON's `result` field is `"id"` now, not `"item"` (ingredients in
  `key` still use `"item"`).
- Fabric API renamed `ItemGroupEvents` to `CreativeModeTabEvents`.
- Dropped the custom tooltip and the enchant-table-strength tweak from
  the 1.21.1 version - both are deprecated/reworked APIs right now and
  weren't essential to the item working. Happy to add them back once the
  core mod is confirmed running.

## Most likely remaining trouble spots

If this doesn't compile or doesn't work quite right, these are the
highest-risk guesses, roughly in order of how unsure I am:

1. `ServerPlayer#teleportTo(...)`'s exact parameter list/order for
   cross-dimension teleports (`TeleportCrystalItem`).
2. `SoundEvents.AMETHYST_BLOCK_CHIME` / `SoundEvents.ENDERMAN_TELEPORT` -
   exact constant names (cosmetic only if wrong - the item still works,
   just quietly, easy one-line fix).
3. The Fabric API package for `CreativeModeTabEvents` (guessed as staying
   under `net.fabricmc.fabric.api.itemgroup.v1`, just the class renamed).
4. Whether `data/minecraft/tags/item/enchantable/durability.json` is
   still the right tag path for Unbreaking/Mending eligibility on 26.3.
5. The pinned Loom/Loader/Fabric API/Gradle versions in
   `gradle.properties` / `build.gradle` / `build.yml` - these change
   fast; check fabricmc.net/develop for exact current numbers.

Paste any build error back and I'll fix the exact line.
