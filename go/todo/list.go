package todo

type Item struct {
	Title       string
	IsCompleted bool
}

type List struct {
	Items []*Item
}

func NewList() *List {
	return &List{make([]*Item, 0)}
}

func (l *List) Add(title string) *List {
	l.Items = append(l.Items, &Item{Title: title})
	return l
}

func (l *List) AddCompleted(title string) *List {
	item := Item{
		Title:       title,
		IsCompleted: true,
	}
	l.Items = append(l.Items, &item)
	return l
}

func (l *List) AllItems() []*Item {
	var result []*Item
	l.forEach(func(item *Item) {
		result = append(result, item)
	})
	return result
}

func (l *List) CompletedItems() []*Item {
	var result []*Item
	l.forEach(func(item *Item) {
		if item.IsCompleted {
			result = append(result, item)
		}
	})
	return result
}

func (l *List) ActiveItems() []*Item {
	var result []*Item
	l.forEach(func(item *Item) {
		if !item.IsCompleted {
			result = append(result, item)
		}
	})
	return result
}

func (l *List) forEach(f func(*Item)) {
	for _, item := range l.Items {
		f(item)
	}
}
