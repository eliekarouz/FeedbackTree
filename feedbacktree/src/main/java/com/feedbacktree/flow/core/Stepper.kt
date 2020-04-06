package com.feedbacktree.flow.core

typealias Stepper<StateT, EventT, OutputT> = (StateT, EventT) -> Step<StateT, OutputT>

/**
 * The idea is to build a bigger [Stepper] out of smaller [Stepper]s.
 * Stepper = Stepper1 + Stepper2 + Stepper3 + ...
 *
 * The [Stepper]s will execute sequentially from left to right.
 *
 * @param StateT
 * @param EventT
 * @param OutputT
 * @param nextStepper in the chain
 * @return A [Stepper] composed of the two [Stepper]s
 */
operator fun <StateT, EventT, OutputT> (Stepper<StateT, EventT, OutputT>).plus(nextStepper: Stepper<StateT, EventT, OutputT>): Stepper<StateT, EventT, OutputT> {
    return { state, event ->
        when (val newStep = this(state, event)) {
            is Step.State -> {
                nextStepper(newStep.state, event)
            }
            is Step.Output -> newStep // No need to call the second stepper as the Flow ended.
        }
    }
}