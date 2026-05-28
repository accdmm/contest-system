@echo off
set MINIO_ROOT_USER=accdmm
set MINIO_ROOT_PASSWORD=lmh050724
start /B "" D:\Minio\bin\minio.exe server D:\develpo\minio\data --console-address "127.0.0.1:9000" --address "127.0.0.1:9005"
