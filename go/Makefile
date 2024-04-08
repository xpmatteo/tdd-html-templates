

test:
	go test ./...

watch:
	@air -v > /dev/null 2> /dev/null || go install github.com/cosmtrek/air@latest
	@air -build.exclude_dir e2e

run:
	go run server.go

open:
	open http://localhost:8080

.PHONY: test watch run open
