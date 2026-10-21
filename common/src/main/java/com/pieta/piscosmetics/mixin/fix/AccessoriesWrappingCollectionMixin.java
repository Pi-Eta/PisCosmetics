package com.pieta.piscosmetics.mixin.fix;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Accessories 1.1.0-beta.53/54 bug: the filtered view it switches an entity's containers to
 * (AccessoriesHolderImpl.validSlotContainers) has an iterator that never returns anything, because hasNext()
 * stores into a local that shadows its field. Anything looping over that entity's containers, including
 * Accessories' own render layer, sees none of them, so every worn item stops drawing until a resync.
 * isEmpty() is also inverted. Both replaced with working versions.
 * Own mixin config with required: false, so a changed Accessories only logs an error
 */
@Pseudo
@Mixin(targets = "io.wispforest.accessories.utils.WrappingCollection", remap = false)
public abstract class AccessoriesWrappingCollectionMixin {

    @Shadow(remap = false) @Final private Collection<Object> innerSet;
    @Shadow(remap = false) @Final private Predicate<Object> keyValidator;

    @Inject(method = "iterator", at = @At("HEAD"), cancellable = true, remap = false)
    private void piscosmetics$workingIterator(CallbackInfoReturnable<Iterator<Object>> cir) {
        Iterator<Object> inner = this.innerSet.iterator();
        Predicate<Object> valid = this.keyValidator;
        cir.setReturnValue(new Iterator<>() {
            private Object next;
            private boolean ready;

            @Override
            public boolean hasNext() {
                while (!ready && inner.hasNext()) {
                    Object candidate = inner.next();
                    if (valid.test(candidate)) {
                        next = candidate;
                        ready = true;
                    }
                }
                return ready;
            }

            @Override
            public Object next() {
                if (!hasNext()) throw new NoSuchElementException();
                ready = false;
                Object result = next;
                next = null;
                return result;
            }
        });
    }

    @Inject(method = "isEmpty", at = @At("HEAD"), cancellable = true, remap = false)
    private void piscosmetics$workingIsEmpty(CallbackInfoReturnable<Boolean> cir) {
        for (Object element : this.innerSet) {
            if (this.keyValidator.test(element)) {
                cir.setReturnValue(false);
                return;
            }
        }
        cir.setReturnValue(true);
    }
}
