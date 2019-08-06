package com.app.nikhil.coroutinedownloader.injection.module

import com.app.nikhil.coroutinedownloader.injection.scope.ActivityScope
import com.app.nikhil.coroutinedownloader.ui.main.MainActivity
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerAppCompatActivity

@Module
abstract class ActivityBindingModule {

  @ActivityScope
  @ContributesAndroidInjector(modules = [MainActivityModule::class])
  internal abstract fun bindMainActivity(): MainActivity
}

@Module
abstract class MainActivityModule {

  @Binds
  @ActivityScope
  abstract fun bindActivity(mainActivity: MainActivity): DaggerAppCompatActivity
}