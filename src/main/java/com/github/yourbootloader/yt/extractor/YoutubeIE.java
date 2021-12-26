package com.github.yourbootloader.yt.extractor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class YoutubeIE extends YoutubeBaseInfoExtractor {

    public static String IE_DESC = "YouTube.com";
    public static List<String> _INVIDIOUS_SITES = Arrays.asList(
            // invidious-redirect websites
            "(?:www\\.)?redirect\\.invidious\\.io",
            "(?:(?:www|dev)\\.)?invidio\\.us",
            // Invidious instances taken from
            // https://github.com/iv-org/documentation/blob/master/Invidious-Instances.md
            "(?:(?:www|no)\\.)?invidiou\\.sh",
            "(?:(?:www|fi)\\.)?invidious\\.snopyta\\.org",
            "(?:www\\.)?invidious\\.kabi\\.tk",
            "(?:www\\.)?invidious\\.13ad\\.de",
            "(?:www\\.)?invidious\\.mastodon\\.host",
            "(?:www\\.)?invidious\\.zapashcanon\\.fr",
            "(?:www\\.)?(?:invidious(?:-us)?|piped)\\.kavin\\.rocks",
            "(?:www\\.)?invidious\\.tinfoil-hat\\.net",
            "(?:www\\.)?invidious\\.himiko\\.cloud",
            "(?:www\\.)?invidious\\.reallyancient\\.tech",
            "(?:www\\.)?invidious\\.tube",
            "(?:www\\.)?invidiou\\.site",
            "(?:www\\.)?invidious\\.site",
            "(?:www\\.)?invidious\\.xyz",
            "(?:www\\.)?invidious\\.nixnet\\.xyz",
            "(?:www\\.)?invidious\\.048596\\.xyz",
            "(?:www\\.)?invidious\\.drycat\\.fr",
            "(?:www\\.)?inv\\.skyn3t\\.in",
            "(?:www\\.)?tube\\.poal\\.co",
            "(?:www\\.)?tube\\.connect\\.cafe",
            "(?:www\\.)?vid\\.wxzm\\.sx",
            "(?:www\\.)?vid\\.mint\\.lgbt",
            "(?:www\\.)?vid\\.puffyan\\.us",
            "(?:www\\.)?yewtu\\.be",
            "(?:www\\.)?yt\\.elukerio\\.org",
            "(?:www\\.)?yt\\.lelux\\.fi",
            "(?:www\\.)?invidious\\.ggc-project\\.de",
            "(?:www\\.)?yt\\.maisputain\\.ovh",
            "(?:www\\.)?ytprivate\\.com",
            "(?:www\\.)?invidious\\.13ad\\.de",
            "(?:www\\.)?invidious\\.toot\\.koeln",
            "(?:www\\.)?invidious\\.fdn\\.fr",
            "(?:www\\.)?watch\\.nettohikari\\.com",
            "(?:www\\.)?invidious\\.namazso\\.eu",
            "(?:www\\.)?invidious\\.silkky\\.cloud",
            "(?:www\\.)?invidious\\.exonip\\.de",
            "(?:www\\.)?invidious\\.riverside\\.rocks",
            "(?:www\\.)?invidious\\.blamefran\\.net",
            "(?:www\\.)?invidious\\.moomoo\\.de",
            "(?:www\\.)?ytb\\.trom\\.tf",
            "(?:www\\.)?yt\\.cyberhost\\.uk",
            "(?:www\\.)?kgg2m7yk5aybusll\\.onion",
            "(?:www\\.)?qklhadlycap4cnod\\.onion",
            "(?:www\\.)?axqzx4s6s54s32yentfqojs3x5i7faxza6xo3ehd4bzzsg2ii4fv2iid\\.onion",
            "(?:www\\.)?c7hqkpkpemu6e7emz5b4vyz7idjgdvgaaa3dyimmeojqbgpea3xqjoid\\.onion",
            "(?:www\\.)?fz253lmuao3strwbfbmx46yu7acac2jz27iwtorgmbqlkurlclmancad\\.onion",
            "(?:www\\.)?invidious\\.l4qlywnpwqsluw65ts7md3khrivpirse744un3x7mlskqauz5pyuzgqd\\.onion",
            "(?:www\\.)?owxfohz4kjyv25fvlqilyxast7inivgiktls3th44jhk3ej3i7ya\\.b32\\.i2p",
            "(?:www\\.)?4l2dgddgsrkf2ous66i6seeyi6etzfgrue332grh2n7madpwopotugyd\\.onion",
            "(?:www\\.)?w6ijuptxiku4xpnnaetxvnkc5vqcdu7mgns2u77qefoixi63vbvnpnqd\\.onion",
            "(?:www\\.)?kbjggqkzv65ivcqj6bumvp337z6264huv5kpkwuv6gu5yjiskvan7fad\\.onion",
            "(?:www\\.)?grwp24hodrefzvjjuccrkw3mjq4tzhaaq32amf33dzpmuxe7ilepcmad\\.onion",
            "(?:www\\.)?hpniueoejy4opn7bc4ftgazyqjoeqwlvh2uiku2xqku6zpoa4bf5ruid\\.onion"
    );
    public static String _VALID_URL = "(?x)^" +
            "                     (" +
            "                         (?:https?://|//)                                    # http(s):// or protocol-independent URL" +
            "                         (?:(?:(?:(?:\\w+\\.)?[yY][oO][uU][tT][uU][bB][eE](?:-nocookie|kids)?\\.com|" +
            "                            (?:www\\.)?deturl\\.com/www\\.youtube\\.com|" +
            "                            (?:www\\.)?pwnyoutube\\.com|" +
            "                            (?:www\\.)?hooktube\\.com|" +
            "                            (?:www\\.)?yourepeat\\.com|" +
            "                            tube\\.majestyc\\.net|" +
            "                            %(invidious)s|" +
            "                            youtube\\.googleapis\\.com)/                        # the various hostnames, with wildcard subdomains" +
            "                         (?:.*?\\#/)?                                          # handle anchor (#/) redirect urls" +
            "                         (?:                                                  # the various things that can precede the ID:" +
            "                             (?:(?:v|embed|e)/(?!videoseries))                # v/ or embed/ or e/" +
            "                             |(?:                                             # or the v= param in all its forms" +
            "                                 (?:(?:watch|movie)(?:_popup)?(?:\\.php)?/?)?  # preceding watch(_popup|.php) or nothing (like /?v=xxxx)" +
            "                                 (?:\\?|\\#!?)                                  # the params delimiter ? or # or #!" +
            "                                 (?:.*?[&;])??                                # any other preceding param (like /?s=tuff&v=xxxx or ?s=tuff&amp;v=V36LpHqtcDY)" +
            "                                 v=" +
            "                             )" +
            "                         ))" +
            "                         |(?:" +
            "                            youtu\\.be|                                        # just youtu.be/xxxx" +
            "                            vid\\.plus|                                        # or vid.plus/xxxx" +
            "                            zwearz\\.com/watch|                                # or zwearz.com/watch/xxxx" +
            "                            %(invidious)s" +
            "                         )/" +
            "                         |(?:www\\.)?cleanvideosearch\\.com/media/action/yt/watch\\?videoId=" +
            "                         )" +
            "                     )?                                                       # all until now is optional -> you can pass the naked ID" +
            "                     (?P<id>[0-9A-Za-z_-]{11})                                # here is it! the YouTube video ID" +
            "                     (?(1).+)?                                                # if we found the ID, everything can follow" +
            "                     $";

    @Autowired
    protected YoutubeIE(YoutubeDLService youtubeDLService) {
        super(youtubeDLService);
    }


}
