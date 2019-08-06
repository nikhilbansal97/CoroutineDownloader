package com.app.nikhil.coroutinedownloader.injection.scope

import androidx.lifecycle.ViewModel
import dagger.MapKey
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass

@Target(FUNCTION)
@Retention(RUNTIME)
@MapKey
annotation class ViewModelKey (val value: KClass<out ViewModel>)