package com.app.nikhil.coroutinedownloader.exceptions

import kotlinx.coroutines.CancellationException

class UserCancelledJobException : CancellationException() {
    override val message: String?
        get() = "User cancelled the Job"
}