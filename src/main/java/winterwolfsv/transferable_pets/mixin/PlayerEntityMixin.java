package winterwolfsv.transferable_pets.mixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void interact(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {

        if (!(entity.isPlayer() && hand.equals(Hand.MAIN_HAND))) return;

        PlayerEntity targetPlayer = (PlayerEntity) entity;
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!(player.isSneaking())) return;
        ServerWorld world = (ServerWorld) player.getWorld();

        for (Entity localEntities : world.getOtherEntities(player, Box.of(player.getPos(), 12, 12, 12), EntityPredicates.VALID_ENTITY)) {
            if (!(localEntities instanceof AnimalEntity animal)) continue;
            if (!(animal.isLeashed() && animal.getHoldingEntity() == player)) continue;
            if (!(animal instanceof TameableEntity pet && pet.getOwner() == player)) continue;

            ((TameableEntity) animal).setOwner(targetPlayer);
            animal.detachLeash(true, false);
            animal.attachLeash(targetPlayer, true);
            showHearts(world, animal);
            showHearts(world, targetPlayer);

            player.sendMessage(targetPlayer.getDisplayName().copy().append(" is now the owner of ").append(animal.getDisplayName()), true);
            targetPlayer.sendMessage(player.getDisplayName().copy().append(" has transferred ").append(animal.getDisplayName()).append(" to you"), true);
        }
        cir.setReturnValue(ActionResult.PASS);
    }

    private void showHearts(ServerWorld world, Entity entity) {
        ParticleEffect particleEffect = ParticleTypes.HEART;
        Random random = new Random();

        for (int i = 0; i < 7; i++) {
            double d = random.nextGaussian() * 0.02;
            double e = random.nextGaussian() * 0.02;
            double f = random.nextGaussian() * 0.02;
            world.spawnParticles(particleEffect, entity.getParticleX(1.0), entity.getRandomBodyY() + 0.5, entity.getParticleZ(1.0), 1, d, e, f, 1);
        }


    }

}


