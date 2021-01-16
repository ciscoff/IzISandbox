package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.sand_box.java;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Здесь BaseItem используется без указания type parameter, хотя является дженериком.
 * В Java это называется Raw Type (https://www.baeldung.com/raw-types-java) и не реконедуется
 * использовать.
 *
 * В Kotlin такой штуки нет, но если хочется, то можно использовать star-projection
 * (https://stackoverflow.com/questions/46828680/how-to-declare-raw-types-in-kotlin), хотя
 * и это не самый лучший вариант.
 *
 * Лучший вариант - это тащить по коду всякие <T>
 */
public abstract class BaseItemController<H extends RecyclerView.ViewHolder, I extends BaseItem> {

}
