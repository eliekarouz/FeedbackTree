You have different ways to write your states, i.e. data classes, sealed classes, enums, and, primitive types. The focus here will be on **sealed classes**.

We will take the example of a simple flow that allows the user to select products loaded from some data source:

```kotlin
sealed class ProductsState {
    object LoadingProducts : ProductsState()

    data class ShowingProducts(
        val allProducts: List<Product>,
        val selectedProducts: Set<Product>
    ) : ProductsState()
}

sealed class ProductsEvent {
    data class LoadedProducts(val products: List<Product>) : ProductsEvent()
    data class SelectedProduct(val product: Product) : ProductsEvent()
    data class DeselecteProduct(val product: Product) : ProductsEvent()
    object ClickedDone : ProductsEvent()
}
```

### Stepper DSL

A stepper is a function that takes the current state your in, and based on the event received, it produces a new state or ends the flow.
FeedbackTree provides a user friendly DSL that allows you to create a stepper when the state is a sealed class. 

```kotlin
stepper = StepperFactory.create<ProductsState, ProductsEvent, Set<Product>> { // 1
    state<ProductsState.LoadingProducts> {
        on<ProductsEvent.LoadedProducts> {
            ProductsState.LoadingProducts.advance() // 2
        }
    }

    state<ProductsState.ShowingProducts> {
        on<ProductsEvent.SelectedProduct> { event ->
            copy( // 3
                selectedProducts = selectedProducts + event.product
            ).advance()
        }
        on<ProductsEvent.DeselecteProduct> { event ->
            copy(
                selectedProducts = selectedProducts - event.product
            ).advance()
        }
        on<ProductsEvent.ClickedDone> {
            endFlowWith(selectedProducts) // 4
        }
    }
}
```

A breakdown of the code above:

1. To create a stepper we need to define the generic arguments `<StateType, EventType, OutputType>`
2. The DSL provides a state and event selectors through the `state<State.SubState>` and `on<SomeEvent>` methods. The `on` lambda expects that you return a **step** through `state.advance()`  or `endFlowWith`
3. Inside the `state<State.SubState>` block you directly have access to the sub-state:
   - `copy()` is actucally `this.copy()` where `this` is of type `State.ShowingProducts`. 
   - You can use the sub-state `ProductsState.ShowingProducts` properties to perform the updates needed like in `selectedProducts + event.product` where `selectedProducts` is just the state before the `ProductsEvent.SelectedProduct`  is emitted.
4. You can complete the flow, when some event is received. 

