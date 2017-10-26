package decka;

public class Hero extends Character {

public Hero (DeckaPlayer playa) {
	isHero = true;
	player = playa;
	health = 32;
	attack = 0;
	healthBase = healthMax = health;
	attackBase = attack;
}

}