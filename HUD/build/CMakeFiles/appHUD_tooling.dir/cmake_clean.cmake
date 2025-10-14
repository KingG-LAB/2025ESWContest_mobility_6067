file(REMOVE_RECURSE
  "HUD_EX/HUD_EX.qrc"
  "HUD_EX/Main.qml"
)

# Per-language clean rules from dependency scanning.
foreach(lang )
  include(CMakeFiles/appHUD_tooling.dir/cmake_clean_${lang}.cmake OPTIONAL)
endforeach()
