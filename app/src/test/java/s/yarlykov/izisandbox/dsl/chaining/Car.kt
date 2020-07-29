package s.yarlykov.izisandbox.dsl.chaining

class Car<T : CarType>(val name: String, val type: T) {
    private var fuel: Int = 0
    private var gears: Int = 4
    private var box : Int = 0
    private var garage : String = ""

    /**
     * Заправить топливом
     */
    fun fillFuelTank(liters: Int){
        fuel += liters
    }

    /**
     * Установить коробку передач
     */
    fun setTransmission(_gears: Int) {
        gears = _gears
    }

    /**
     * Бокс в гараже
     */
    fun stayInBox(num : Int) {
        box = num
    }

    /**
     * Название гаража
     */
    fun goToGarage(_garage : String) {
        garage = _garage
    }

    override fun toString(): String {
        return "I'm $name, has $fuel liters of fuel and live in $garage box $box"
    }
}
