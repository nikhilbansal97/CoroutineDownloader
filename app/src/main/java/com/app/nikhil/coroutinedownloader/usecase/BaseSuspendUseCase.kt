package com.app.nikhil.coroutinedownloader.usecase

interface BaseSuspendUseCase<out T, in U> {
  suspend fun perform(param: U): T
}