/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.flameyheart.playroom.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;

public final class LivingEntityEvents {

    public static final Event<StartTick> START_TICK = EventFactory.createArrayBacked(StartTick.class, (callbacks) -> (server) -> {
        for (StartTick event : callbacks) {
            event.onStartTick(server);
        }
    });

    public static final Event<EndTick> END_TICK = EventFactory.createArrayBacked(EndTick.class, (callbacks) -> (server) -> {
        for (EndTick callback : callbacks) {
            callback.onEndTick(server);
        }
    });

    public static final Event<StartTick> START_BASE_TICK = EventFactory.createArrayBacked(StartTick.class, (callbacks) -> (server) -> {
        for (StartTick event : callbacks) {
            event.onStartTick(server);
        }
    });

    public static final Event<EndTick> END_BASE_TICK = EventFactory.createArrayBacked(EndTick.class, (callbacks) -> (server) -> {
        for (EndTick callback : callbacks) {
            callback.onEndTick(server);
        }
    });

    public static final Event<StartTravel> START_TRAVEL = EventFactory.createArrayBacked(StartTravel.class, (callbacks) -> (server) -> {
        for (StartTravel event : callbacks) {
            event.onStartTravel(server);
        }
    });

    public static final Event<EndTravel> END_TRAVEL = EventFactory.createArrayBacked(EndTravel.class, (callbacks) -> (server) -> {
        for (EndTravel callback : callbacks) {
            callback.onEndTravel(server);
        }
    });

    @FunctionalInterface
    public interface StartTick {
        void onStartTick(LivingEntity entity);
    }

    @FunctionalInterface
    public interface EndTick {
        void onEndTick(LivingEntity entity);
    }

    @FunctionalInterface
    public interface StartTravel {
        void onStartTravel(LivingEntity entity);
    }

    @FunctionalInterface
    public interface EndTravel {
        void onEndTravel(LivingEntity entity);
    }
}
