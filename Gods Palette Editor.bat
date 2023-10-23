@echo off
set DEBUG=xxx
set GODS_ROOT_DIR=%~dp0

java -cp "%GODS_ROOT_DIR%bin\gods-deluxe.jar" gods.editor.palette.GodsPaletteEditor
