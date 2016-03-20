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

import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Vector3f;

/**
 * Non player characters should have this component even if they don't move, so that movement input gets simulated fo
 * them. Otherwise the game will try to predict the character movmeent based on the last movement which leads to strange
 * behavior.
 */
public class NPCMovementComponent implements Component {
    /**
     * Angle in degree in which the character will look at. Used to simulate constant yaw input. Calculating it
     * freshly each time would lead to rounding mistakes that commulate.
     */
    public float yaw;

    /**
     * Position to which the charcter will move to in a straight line. For advanced pathing, a behavior tree should
     * be used that sets this field to the next waypoint of the calculated path to the target.
     */
    public transient Vector3f targetPosition;
}
