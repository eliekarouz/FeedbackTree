/*
 * Created by eliek on 12/31/2020
 * Copyright (c) 2020 eliekarouz. All rights reserved.
 */

package com.feedbacktree.howtoguides

import com.feedbacktree.flow.core.StepperFactory
import com.feedbacktree.flow.core.advance
import com.feedbacktree.flow.core.endFlowWith

data class Product(val name: String, val price: Int)

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

val stepper = StepperFactory.create<ProductsState, ProductsEvent, Set<Product>> { // 1
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