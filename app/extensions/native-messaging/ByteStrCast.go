package main

import (
	"reflect"
	"unsafe"
)

func B2S(b *[]byte) string {
	return *(*string)(unsafe.Pointer(b))
}

func S2B(s* string) (b* []byte) {
	bh := (*reflect.SliceHeader)(unsafe.Pointer(b))
	sh := *(*reflect.StringHeader)(unsafe.Pointer(s))
	bh.Data = sh.Data
	bh.Len = sh.Len
	bh.Cap = sh.Len
	return b
}
