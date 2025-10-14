# Additional clean files
cmake_minimum_required(VERSION 3.16)

if("${CONFIG}" STREQUAL "" OR "${CONFIG}" STREQUAL "Debug")
  file(REMOVE_RECURSE
  "CMakeFiles\\appHUD_autogen.dir\\AutogenUsed.txt"
  "CMakeFiles\\appHUD_autogen.dir\\ParseCache.txt"
  "appHUD_autogen"
  )
endif()
