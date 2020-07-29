package s.yarlykov.izisandbox.dsl.chaining

/**
 * Это template для контейнера. Здесь есть методы создания вложенных элементов
 * и методы их инициализации chaining'ом.
 */

open class Garage {

    /**
     * Функции для создания разных типов машин. Это просто функции для создания инстансов.
     */
    fun familyCar(name : String) : Car<CarType> {
        return Car(name, CarType.Hatchback)
    }

    fun businessCar(name : String) : Car<CarType> {
        return Car(name, CarType.Sedan)
    }

    fun truckCar(name : String) : Car<CarType> {
        return Car(name, CarType.Truck)
    }

    /**
     * Функции для настройки опций машин. Обрати внимание, что это полноценные функции,
     * которые имеют оператор return.
     */
    fun <T : CarType> Car<T>.placeInBox(box : Int) :  Car<T> {
        this.stayInBox(box)
        return this
    }

    fun <T : CarType> Car<T>.changeTransmission(gears : Int) : Car<T> {
        this.setTransmission(gears)
        return this
    }

    fun <T : CarType> Car<T>.addFuel(liters : Int) : Car<T> {
        this.fillFuelTank(liters)
        return this
    }

    fun <T : CarType> Car<T>.putInGarage(garage : String) : Car<T> {
        this.goToGarage(garage)
        return this
    }
}