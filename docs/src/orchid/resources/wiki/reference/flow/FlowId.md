---
title: Flow Id
category: Flow
order: 4
---

### Setting Flow Identifiers

When initiating a Flow, it's essential to designate an identifier id for the Flow. This identifier
can be a constant value or dynamically generated based on input.

```kotlin
Flow(
    id = "MyFlowFixedId",
    // ...
)

Flow(
    id = { input -> "MyFlowFor-${input.itemKey}" },
    // ...
)
```

### Ensuring Uniqueness

When applying identifiers to your flows, it's not only important to aim for a reasonable level of
uniqueness, but it's also critical to ensure **complete uniqueness** within the specific context of
the `render` block. This consideration becomes particularly significant when dealing with the
initiation of multiple child flows.

In scenarios where you find yourself simultaneously initiating multiple child flows using
the `context.renderChild(input, ChildFlow, ...)` function, it becomes paramount that each flow
instance possesses an identifier that is distinct from all others. This distinction is necessary to
prevent collisions and unexpected interactions between running flows.

The `renderChild` process begins by computing the identifier of the child flow based on the provided
input. Subsequently, this identifier plays a crucial role in determining whether a specific child
flow needs to be newly initiated or resumed from a prior state within the same render block. The
decision is made based on whether the computed identifier matches any previously initiated flows
within the current rendering context. A new flow instance is only created if the computed identifier
differs from those encountered so far.

### Practical Use of Unique IDs for Each Input

Consider an example involving an `ItemsListFlow` and `ItemDetailFlow`. The `ItemsListFlow` maintains
a state variable called `showingDetailsItemKey: String?`. When this variable isn't null, the render
block triggers the `ItemDetailFlow`.

While a fixed ID works well on mobiles, envision a tablet's split-screen layout. Here's where the
challenge arises. The `ItemDetailFlow` remains active, but as you select new items, it needs to
restart. Using a fixed ID for `ItemDetailFlow` causes the initial flow to resume, preventing new
starts.

The remedy? Incorporate the `itemKey` into the Flow's ID. This guarantees a fresh flow for each
item, ensuring that selecting a different item leads to a new flow initiation.
