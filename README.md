# DISPLAY YOUR FAVORITE GRAPHICS IN YOUR COMMAND LINE
> Using the power of duke!


```
./gradlew run -PappArgs="['--width','13','/home/mario/Pictures/tux.png']"
```

or

```
./gradlew assemble
java -jar build/libs/duke.jar /usr/share/pixmaps/faces/lightning.jpg --width 30
```

will output this beautiful image

<img src="./assets/1.png" width=100/>

# Displaying a simple raycasting 3d env _WITH TEXTURES_

```
./gradlew -Dorg.gradle.java.home=../jdk-12 assemble
```

and

```
../jdk-12/bin/java -jar build/libs/duke.jar 3d level
```


Enjoy


PS: You might be interested in
```
alias duke='~/Applications/jdk-12/bin/java -jar ~/Projects/duke/build/libs/duke.jar --height $(tput lines)'
```
