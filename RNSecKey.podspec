require 'json'
version = JSON.parse(File.read('package.json'))["version"]

Pod::Spec.new do |s|

  s.name           = "RNSecKey"
  s.version        = version
  s.summary        = "React Native SecKey"
  s.license        = "MIT"
  s.author         = "bktan"
  s.platform       = :ios, "10.0"
  s.homepage       = "http://192.168.0.92:8008/app-git/Repository/RNSecKey"
  s.source         = { :git => "http://192.168.0.92:8008/app-git/RNSecKey.git" }
  s.source_files   = 'ios/*.{h,m,swift}'
  # s.preserve_paths = "**/*.js"

end
