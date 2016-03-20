/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.gooeysQuests;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.registry.In;

/**
 * Makes entities with the {@link NPCMovementSystem} move to the target specified by {@link NPCMovementComponent}.
 *
 * The rotation gets taken from {@link NPCMovementComponent}.
 *
 * It is just a proof of concept and will propably be generalized and moved to either pathfinding or engine.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class NPCMovementSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final float MIN_DISTANCE = 0.1f;


    @In
    private EntityManager entityManager;


    @Override
    public void update(float delta) {
        Iterable<EntityRef> entities = entityManager.getEntitiesWith(NPCMovementComponent.class);
        for (EntityRef entity: entities) {
            updateEntitiy(entity, delta);
        }
    }

    private void updateEntitiy(EntityRef entity, float delta) {
        NPCMovementComponent npcMovementComponent = entity.getComponent(NPCMovementComponent.class);
        Vector3f currentTarget = null;
        if (npcMovementComponent != null && npcMovementComponent.targetPosition != null ) {
            currentTarget = npcMovementComponent.targetPosition;
        }
        boolean jumpRecommended = false;
        MinionMoveComponent mnionMovementComponent = entity.getComponent(MinionMoveComponent.class);
        if (mnionMovementComponent != null && mnionMovementComponent.horizontalCollision) {
            mnionMovementComponent.horizontalCollision = false;
            entity.saveComponent(mnionMovementComponent);
            jumpRecommended = true;
        }
        sendInputEvent(entity, currentTarget, jumpRecommended, delta);

    }

    private void sendInputEvent(EntityRef entity, Vector3f currentTarget, boolean jumpRecommended, float delta) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        CharacterMoveInputEvent inputEvent = null;
        if (currentTarget != null) {
            Vector3f worldPos = new Vector3f(location.getWorldPosition());
            Vector3f targetDirection = new Vector3f();
            targetDirection.sub(currentTarget, worldPos);
            float yaw = (float) Math.atan2(targetDirection.x, targetDirection.z);

            Vector3f drive = new Vector3f();
            float dist = MIN_DISTANCE;
            if (targetDirection.x * targetDirection.x + targetDirection.z * targetDirection.z > dist * dist) {
                targetDirection.scale(0.5f);
                drive.set(targetDirection);
                boolean jumpRequested = jumpRecommended;
                float requestedYaw = 180f + yaw * TeraMath.RAD_TO_DEG;
                 inputEvent = new CharacterMoveInputEvent(0, 0, requestedYaw, drive, false,
                        jumpRequested, (long) (delta * 1000));
            }
        }

        // send default input event, as otherwise an interpolation of last movement gets performed
        if (inputEvent == null) {
            Vector3f drive = new Vector3f(0.0f, 0.0f, 0.0f);
            NPCMovementComponent npcMovementComponent = entity.getComponent(NPCMovementComponent.class);
            float yaw = npcMovementComponent.yaw;;
            boolean jumpRequested = false;
            inputEvent = new CharacterMoveInputEvent(0, 0, yaw, drive, false,
                    jumpRequested, (long) (delta * 1000));
        }

        entity.send(inputEvent);
    }


}
