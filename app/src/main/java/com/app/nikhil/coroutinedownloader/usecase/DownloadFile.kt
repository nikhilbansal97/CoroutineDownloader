package com.app.nikhil.coroutinedownloader.usecase

import javax.inject.Inject

class DownloadFile @Inject constructor() : BaseUseCase() {
  operator fun invoke(url: String) {}
}