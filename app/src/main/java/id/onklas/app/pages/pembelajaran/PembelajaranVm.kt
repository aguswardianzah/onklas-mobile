package id.onklas.app.pages.pembelajaran

import androidx.lifecycle.ViewModel
import com.squareup.moshi.Moshi
import id.onklas.app.R
import id.onklas.app.utils.PreferenceClass
import javax.inject.Inject

class PembelajaranVm @Inject constructor(
    val pref: PreferenceClass,
    val moshi: Moshi
) : ViewModel() {

    val listBanner by lazy { listOf(R.drawable.banner_1, R.drawable.banner_2, R.drawable.banner_3) }
    val listEdu by lazy {
        listOf(
            R.drawable.onklas_edu1,
            R.drawable.onklas_edu2,
            R.drawable.onklas_edu3,
            R.drawable.onklas_edu4,
            R.drawable.onklas_edu5,
            R.drawable.onklas_edu6,
            R.drawable.onklas_edu7,
            R.drawable.onklas_edu8
        )
    }

//    val listHeadItem by lazy {
//        listOf(
//            Triple(
//                "https://liquipedia.net/commons/images/f/fa/Abaddon_Large.png",
//                "Abaddon, the Lord of Avernus",
//                "The Font of Avernus is the source of a family’s strength, a crack in primal stones from which vapors of prophetic power have issued for generations. Each newborn of the cavernous House Avernus is bathed in the black mist, and by this baptism they are given an innate connection to the mystic energies of the land. They grow up believing themselves fierce protectors of their lineal traditions, the customs of the realm—but what they really are protecting is the Font itself. And the motives of the mist are unclear."
//            ), Triple(
//                "https://liquipedia.net/commons/images/1/11/Alchemist_Large.png",
//                "Razzil Darkbrew, the Alchemist",
//                "The sacred science of Chymistry was a Darkbrew family tradition, but no Darkbrew had ever shown the kind of creativity, ambition, and recklessness of young Razzil. However, when adulthood came calling he pushed aside the family trade to try his hand at manufacturing gold through Alchemy. In an act of audacity befitting his reputation, Razzil announced he would transmute an entire mountain into gold. Following two decades of research and spending and preparation, he failed spectacularly, quickly finding himself imprisoned for the widespread destruction his experiment wrought. Yet Razzil was never one to take a setback lightly, and sought escape to continue his research. When his new cellmate turned out to be a fierce ogre, he found just the opportunity he needed. After convincing the ogre not to eat him, Razzil set about carefully concocting a tincture for it to drink, made from the moulds and mosses growing in the prison stone work. In a week's time, it seemed ready. When the ogre drank the potion, it flew into an unstoppable berserker rage, destroying the cell bars and exploding through walls and guards alike. They soon found themselves lost somewhere in the forest surrounding the city with a trail of wreckage in their wake and no signs of pursuit. In the tonic's afterglow, the ogre seemed serene, happy, and even eager. Resolving to work together, the pair set off to collect the materials needed to attempt Razzil's Alchemic transmutation once more."
//            ), Triple(
//                "https://liquipedia.net/commons/images/a/a1/Axe_Large.png",
//                "Mogul Khan, the Axe",
//                "As a grunt in the Army of Red Mist, Mogul Khan set his sights on the rank of Red Mist General. In battle after battle he proved his worth through gory deed. His rise through the ranks was helped by the fact that he never hesitated to decapitate a superior. Through the seven year Campaign of the Thousand Tarns, he distinguished himself in glorious carnage, his star of fame shining ever brighter, while the number of comrades in arms steadily dwindled. On the night of ultimate victory, Axe declared himself the new Red Mist General, and took on the ultimate title of 'Axe.' But his troops now numbered zero. Of course, many had died in battle, but a significant number had also fallen to Axe's blade. Needless to say, most soldiers now shun his leadership. But this matters not a whit to Axe, who knows that a one-man army is by far the best."
//            ), Triple(
//                "https://liquipedia.net/commons/images/1/19/Beastmaster_Large.png",
//                "Karroch, the Beastmaster",
//                "Karroch was born a child of the stocks. His mother died in childbirth; his father, a farrier for the Last King of Slom, was trampled to death when he was five. Afterward Karroch was indentured to the king's menagerie, where he grew up among all the beasts of the royal court: lions, apes, fell-deer, and things less known, things barely believed in. When the lad was seven, an explorer brought in a beast like none before seen. Dragged before the King in chains, the beast spoke, though its mouth moved not. Its words: a plea for freedom. The King only laughed and ordered the beast perform for his amusement; and when it refused, struck it with the Mad Scepter and ordered it dragged to the stocks."
//            ), Triple(
//                "https://liquipedia.net/commons/images/0/0b/Brewmaster_Large.png",
//                "Mangix, the Brewmaster",
//                "Deep in the Wailing Mountains, in a valley beneath the Ruined City, the ancient Order of the Oyo has for centuries practiced its rites of holy reverie, communing with the spirit realm in grand festivals of drink. Born to a mother's flesh by a Celestial father, the youth known as Mangix was the first to grow up with the talents of both lineages. He trained with the greatest aesthetes of the Order, eventually earning, through diligent drunkenness, the right to challenge for the title of Brewmaster—that appellation most honored among the contemplative malt-brewing sect."
//            ), Triple(
//                "https://liquipedia.net/commons/images/f/f9/Bristleback_Large.png",
//                "Rigwarl, the Bristleback",
//                "Never one to turn his back on a fight, Rigwarl was known for battling the biggest, meanest scrappers he could get his hands on. Christened Bristleback by the drunken crowds, he waded into backroom brawls in every road tavern between Slom and Elze, until his exploits finally caught the eye of a barkeep in need of an enforcer. For a bit of brew, Bristleback was hired to collect tabs, keep the peace, and break the occasional leg or two (or five, in the case of one unfortunate web-hund)."
//            ), Triple(
//                "https://liquipedia.net/commons/images/5/54/Centaur_Warrunner_Large.png",
//                "Bradwarden, the Centaur Warrunner",
//                "It's said that a centaur's road is paved with the corpses of the fallen. For the one called Warrunner, it has been a long road indeed. To outsiders, the four-legged clans of Druud are often mistaken for simple, brutish creatures. Their language has no written form; their culture lacks pictographic traditions, structured music, formalized religion. For centaurs, combat is the perfect articulation of thought, the highest expression of self. If killing is an art among centaurs, then Bradwarden the Warrunner is their greatest artist. He rose to dominance on the proving grounds of Omexe, an ancient arena where centaur clans have for millennia gathered to perform their gladiatorial rights. As his fame spread, spectators came from far and wide to see the great centaur in action. Always the first to step into the arena, and the last to leave, he composes a masterpiece in each guttering spray, each thrust of blood-slickened blade-length. It is the poetry of blood on steel, flung in complex patterns across the pale sands of the killing floor. Warrunner defeated warrior after warrior, until the arena boomed with the cheering of his name, and he found himself alone, the uncontested champion of his kind. The great belt of Omexe was bestowed, wrapped around his broad torso, but in his victory, the death-artist found only emptiness. For what is a warrior without a challenge? The great centaur galloped out of Omexe that day with a new goal. To his people, Warrunner is the greatest warrior to ever step into the arena. Now he has set out to prove he is the greatest fighter who has ever lived."
//            ), Triple(
//                "https://liquipedia.net/commons/images/4/46/Chaos_Knight_Large.png",
//                "Chaos Knight",
//                "The veteran of countless battles on a thousand worlds, Chaos Knight hails from a far upstream plane where the fundamental laws of the universe have found sentient expression. Of all the ancient Fundamentals, he is the oldest and most tireless—endlessly searching out a being he knows only as \"The Light.\" Long ago the Light ventured out from the progenitor realm, in defiance of the first covenant. Now Chaos Knight shifts from plane to plane, always on the hunt to extinguish the Light wherever he finds it. A thousand times he has snuffed out the source, and always he slides into another plane to continue his search anew."
//            ), Triple(
//                "https://liquipedia.net/commons/images/7/70/Anti-Mage_Large.png",
//                "Anti-Mage",
//                "The monks of Turstarkuri watched the rugged valleys below their mountain monastery as wave after wave of invaders swept through the lower kingdoms. Ascetic and pragmatic, in their remote monastic eyrie they remained aloof from mundane strife, wrapped in meditation that knew no gods or elements of magic. Then came the Legion of the Dead God, crusaders with a sinister mandate to replace all local worship with their Unliving Lord's poisonous nihilosophy. From a landscape that had known nothing but blood and battle for a thousand years, they tore the souls and bones of countless fallen legions and pitched them against Turstarkuri. The monastery stood scarcely a fortnight against the assault, and the few monks who bothered to surface from their meditations believed the invaders were but demonic visions sent to distract them from meditation. They died where they sat on their silken cushions. Only one youth survived—a pilgrim who had come as an acolyte, seeking wisdom, but had yet to be admitted to the monastery. He watched in horror as the monks to whom he had served tea and nettles were first slaughtered, then raised to join the ranks of the Dead God's priesthood. With nothing but a few of Turstarkuri's prized dogmatic scrolls, he crept away to the comparative safety of other lands, swearing to obliterate not only the Dead God's magic users—but to put an end to magic altogether."
//            ), Triple(
//                "https://liquipedia.net/commons/images/6/60/Arc_Warden_Large.png",
//                "Arc Warden",
//                "Before the beginning of all, there existed a presence: a primordial mind, infinite, awesome, and set to inscrutable purpose. As the universe thundered into being, this mind was fragmented and scattered. Two among its greater fragments—who would come to be named Radiant and Dire—found themselves locked in vicious opposition, and began twisting all of creation to serve their conflict."
//            ), Triple(
//                "https://liquipedia.net/commons/images/a/a6/Bloodseeker_Large.png",
//                "Bloodseeker",
//                "Strygwyr the Bloodseeker is a ritually sanctioned hunter, Hound of the Flayed Twins, sent down from the mist-shrouded peaks of Xhacatocatl in search of blood. The Flayed Ones require oceanic amounts of blood to keep them sated and placated, and would soon drain their mountain empire of its populace if the priests of the high plateaus did not appease them. Strygwyr therefore goes out in search of carnage. The vital energy of any blood he lets, flows immediately to the Twins through the sacred markings on his weapons and armor. Over the years, he has come to embody the energy of a vicious hound; in battle he is savage as a jackal. Beneath the Mask of the Bloodseeker, in the rush of bloody quenching, it is said that you can sometime see the features of the Flayers taking direct possession of their Hound."
//            )
//        )
//    }
}