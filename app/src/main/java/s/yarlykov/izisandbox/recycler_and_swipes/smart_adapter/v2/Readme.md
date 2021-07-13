# Agenda:

### H - BaseViewHolder
### I - BaseItem<H>
### T - model data type

## BaseItem:
	1. Является контейнером для элемента данных (T) и контроллера BaseController

## BaseController:
	1. Должен создать ViewHolder (H)
	2. Должен "связать" ViewHolder (H) и BaseItem (I)
	
Поэтому: 
	BaseController является дженериком от <H, I>, а точнее BaseController<BaseViewHolder, BaseItem<H>>.
	А так как BaseItem имеет ссылку на BaseController, то он тоже BaseItem<H>.

## NOTE: 
### `Контроллер вообще не имеет никаких зависимостей и никакого состояния. Это просто набор методов,`
### `манипулирующих аргументами. Можно даже считать, что это "статические" методы. Все от чего зависит`
### `конкретный контроллер - это фактические type parameters с которыми работают его методы.`


## Алгоритм работы адаптера:
### ------------------------------

#### `Список контроллеров для текущей модели данных`

```kotlin
val supportedControllers: SparseArray<BaseController<*, *>>
```

#### `Модель данных`

```kotlin
val model: List<BaseItem>
```

#### `Что имеет BaseItem`
BaseItem:
 --> T
 --> Controller

Когда адаптеру нужно создать новый ViewHolder, то он делает так:

```kotlin
override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
    return supportedControllers[viewType].createViewHolder(parent).apply {
        eventsObservable.subscribe(events)
    }
}
```

Когда адаптеру нужно "наполнить" новый ViewHolder данными, то он делает так:

```kotlin
override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
	val baseItem = model[index]
	holder = baseItem.controller.createViewHolder
	baseItem.bind(holder, baseItem)
	
	// и потом внутри baseItem::bind произойдет биндинг вот таким образом
	// holder.bind(baseItem.data)
}
```

BaseItem
 --> BaseController (должен знать типы)
		::createViewHolder
		::bind(H, I)
		::viewType() : Int
		
 
BindableItem
  --> T
  --> BindableItemController 


  
  
  