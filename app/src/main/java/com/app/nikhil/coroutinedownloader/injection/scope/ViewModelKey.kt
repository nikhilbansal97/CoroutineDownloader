package com.app.nikhil.coroutinedownloader.injection.scope

import androidx.lifecycle.ViewModel
import dagger.MapKey
import javax.inject.Scope
import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass

@Scope
@Target(FUNCTION)
@Retention(SOURCE)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)