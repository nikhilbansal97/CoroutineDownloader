package com.app.nikhil.coroutinedownloader.injection.module

import com.app.nikhil.coroutinedownloader.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingModule {

  @ContributesAndroidInjector
  abstract fun provideMainActivity(): MainActivity

}