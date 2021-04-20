package s.yarlykov.izisandbox.izilogin.repo

interface AuthRepo {
    fun authenticate(login: String, password: String) : Boolean
}