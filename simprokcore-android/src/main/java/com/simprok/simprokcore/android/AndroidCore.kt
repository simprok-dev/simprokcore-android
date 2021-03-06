//
//  Core.kt
//  simprokcore
//
//  Created by Andrey Prokhorenko on 12.03.2022.
//  Copyright (c) 2022 simprok. All rights reserved.

package com.simprok.simprokcore.android

import com.simprok.simprokmachine.android.*
import com.simprok.simprokmachine.api.Direction
import com.simprok.simprokmachine.api.Ward
import com.simprok.simprokmachine.api.inward
import com.simprok.simprokmachine.api.outward
import com.simprok.simprokmachine.machines.Machine

/**
 * A RootMachine interface that describes all the layers of the application.
 */
interface AndroidCore<State, Event> : AndroidRootMachine<StateAction<Event>, StateAction<Event>> {

    /**
     * Application's layers that receive the latest state and handle it via their
     * mappers as well as emit events that are handled by their reducers.
     */
    val layers: Set<ComponentLayer<Event>>

    fun reduce(state: State?, event: Event): ReducerResult<State>

    override val component: ComponentMachine<StateAction<Event>, StateAction<Event>>
        get() {
            val reducer: Machine<StateAction<Event>, StateAction<Event>> =
                CoreReducerMachine<Event, State> { state, event ->
                    reduce(state, event)
                }.outward {
                    Ward.set<StateAction<Event>>(StateAction.DidUpdate(it))
                }.inward {
                    when (it) {
                        is StateAction.WillUpdate<Event> -> Ward.set(it.event)
                        is StateAction.DidUpdate<Event> -> Ward.set()
                    }
                }

            return mergeList(layers.map { it.child }).mergeWith(reducer).redirect {
                Direction.Back(it)
            }
        }
}