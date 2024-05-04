package todo

import "math/rand"

type Item struct {
	Id          int
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
	item := Item{
		Id:    generateRandomId(),
		Title: title,
	}
	l.Items = append(l.Items, &item)
	return l
}

func (l *List) AddCompleted(title string) *List {
	item := Item{
		Id:          generateRandomId(),
		Title:       title,
		IsCompleted: true,
	}
	l.Items = append(l.Items, &item)
	return l
}

func (l *List) AddWithId(id int, title string) *List {
	item := Item{
		Id:    id,
		Title: title,
	}
	l.Items = append(l.Items, &item)
	return l
}

func generateRandomId() int {
	return abs(rand.Int())
}

func abs(n int) int {
	if n < 0 {
		return -n
	}
	return n
}

func (l *List) AllItems() []*Item {
	var result []*Item
	for _, item := range l.Items {
		result = append(result, item)
	}
	return result
}

func (l *List) CompletedItems() []*Item {
	var result []*Item
	for _, item := range l.Items {
		if item.IsCompleted {
			result = append(result, item)
		}
	}
	return result
}

func (l *List) ActiveItems() []*Item {
	var result []*Item
	for _, item := range l.Items {
		if !item.IsCompleted {
			result = append(result, item)
		}
	}
	return result
}
