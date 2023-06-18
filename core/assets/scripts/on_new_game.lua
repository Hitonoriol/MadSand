utils:showDialog([[
	/Defaults {
		/FadeIn(0) /FadeOut(0)
		/BackgroundOpacity(0.0) /StageOpacity(1.0)
	} Defaults
	
	You wake up in an unfamiliar place, unable to recall how you ended up there or even who you are. You can't shake off the confusion clouding your mind.
	/Button(What? Who am I?)
	=>
	
	As you try to make sense of your surroundings, you realize that your vision is unfocused, and an eerie silence envelops the air. A pounding headache intensifies, accompanied by waves of nausea.
	/Button(Am I Dead?)
	=>
	
	In an attempt to ground yourself, you pinch your arm and are met with a sharp pain. Relief washes over you as you realize that this is not a dream, and you still possess a physical body.
	/Button(Did I get kidnapped?)
	=>
	
	/FadeOut(0.5)
	A minute passes, and your vision gradually clears. You find yourself in a dense forest, far away from any signs of civilization.
	{p}You notice a man who seems to be deliberately avoiding your gaze. Your headache is only getting worse, so this man is your only chance to get any help.
	/Button(Should I try talking to him?)
]]);

local fx = utils:getWorldEffects();
fx:enableEffect("BlackAndWhiteEffect");
fx:enableEffect("FilmGrainEffect");