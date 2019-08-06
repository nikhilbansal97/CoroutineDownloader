package com.app.nikhil.coroutinedownloader.injection.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.nikhil.coroutinedownloader.injection.scope.ViewModelKey
import com.app.nikhil.coroutinedownloader.ui.base.ViewModelFactory
import com.app.nikhil.coroutinedownloader.ui.main.MainViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelBindingModule {

  @Binds
  @IntoMap
  @ViewModelKey(MainViewModel::class)
  abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel

  @Binds
  abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}