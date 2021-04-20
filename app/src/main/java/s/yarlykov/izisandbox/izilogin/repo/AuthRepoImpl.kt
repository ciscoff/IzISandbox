package s.yarlykov.izisandbox.izilogin.repo

import javax.inject.Inject

class AuthRepoImpl @Inject constructor() : AuthRepo {

    override fun authenticate(login: String, password: String): Boolean {
        return login.length > 4 && password.length > 6
    }
}