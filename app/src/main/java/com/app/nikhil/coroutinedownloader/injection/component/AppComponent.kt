package com.app.nikhil.coroutinedownloader.injection.component

import android.content.Context
import com.app.nikhil.coroutinedownloader.MainApplication
import com.app.nikhil.coroutinedownloader.injection.module.AppModule
import com.app.nikhil.coroutinedownloader.injection.module.ViewModelBindingModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, AppModule::class, ViewModelBindingModule::class])
interface AppComponent : AndroidInjector<MainApplication> {

  /*
  * Customize the builder generated by the dagger compiler
  */
  @Component.Builder
  abstract class Builder : AndroidInjector.Builder<MainApplication>() {
    @BindsInstance
    abstract fun appContext(context: Context)

    override fun seedInstance(instance: MainApplication) {
      appContext(instance.applicationContext)
    }
  }
}