package studio.lunabee.onesafe.extension

import android.util.Base64
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.coreui.R
import studio.lunabee.onesafe.model.OSItemIllustration
import kotlin.random.Random

val LoremIpsum.string: String
    get() = values.joinToString()

fun loremIpsum(words: Int): String {
    return LoremIpsum(words = words).string
}

fun loremIpsumSpec(words: Int): LbcTextSpec {
    return LbcTextSpec.Raw(LoremIpsum(words = words).string)
}

val iconSample: ByteArray
    get() = Base64.decode(
        "UklGRuYVAABXRUJQVlA4WAoAAAAwAAAAxwAAxwAASUNDUBgCAAAAAAIYAAAAAAIQAABtbnRyUkdCIFhZWiAAAAAAAAAAAAAAAABhY3NwAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAQAA9tYAAQAAAADTLQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAlkZXNjAAAA8AAAAHRyWFla" +
            "AAABZAAAABRnWFlaAAABeAAAABRiWFlaAAABjAAAABRyVFJDAAABoAAAAChnVFJDAAABoAAAAChiVFJDAAABoAAAACh3dHB0AAAByAAAABRjcHJ0AAAB3AAAADxt" +
            "bHVjAAAAAAAAAAEAAAAMZW5VUwAAAFgAAAAcAHMAUgBHAEIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAFhZWiAAAAAAAABvogAAOPUAAAOQWFlaIAAAAAAAAGKZAAC3hQAAGNpYWVogAAAAAAAAJKAAAA+EAAC2z3BhcmEAAAAAAAQA" +
            "AAACZmYAAPKnAAANWQAAE9AAAApbAAAAAAAAAABYWVogAAAAAAAA9tYAAQAAAADTLW1sdWMAAAAAAAAAAQAAAAxlblVTAAAAIAAAABwARwBvAG8AZwBsAGUAIABJ" +
            "AG4AYwAuACAAMgAwADEANlZQOEyoEwAAL8fAMRANdSGi/wFPt7Ytbqtt23bIsuxw1sXMzBDOIncvXiD74utqQRd6Vys/4MIuM8i1GJthlZKLmRmCF8UOWLLrGL5E" +
            "Ved5nEd1vWnb9rZttm3rAVIU3atMi6TcbQJqaRPI3wwgM8iScXgcWTKDDCC/rjKBKpmuy2VVKqadXlQJHH62bVvmurat83lLzLKlkt2ZmX+Bj571uHfHncWdMdOh" +
            "vHPvqq/MHLm31E47pszMoFZqXfUJjlai96GA//95tXpn85zak3l9M2YzzLayZmS7KW67s+0t12zW9e2cdJ+ddNIyYcG2VbW5422UKNnHJsK9kMc3yAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAn1xqdmVmYgznSrFGnIUFQFEUgmQKCgKQGGqAEqgAAQA/0wzdC" +
            "N0mqEdJGU6UkadGKr4haqNloZ80tm1ttAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARo/9/pOvCvvt" +
            "OTuy2jSatCmKYiuZVqAzlQZUJzLkZdOqEaJGUyuKIiqJUtmanX3Z8/pzLZ7tu/zl83LPHA9tGs31ZJHjVUwE0wB4oCOZzsPj8rKpFflCxSuNeP731XsrFr97cNij" +
            "ku5Kz5q6zJNIN9Jk0AjKCaYqVgMAfMur5qFm5NOqBYfG21W+1PvcjcOpRXQWgRtYR+EFDZK7yCwgL5v/IL+RN3f+sr72xIlqVcicffytSUV7fA2Zy0AYExusIRiA" +
            "lrzq5zVmV5f2vfnkc5Gjya+dGj1u2DTaqhFbwDw+OwjJ9AN6ZU8smafWTZ28+dVHvib9PH7OxsBTI7zADeSMKABzjYiD4bxs7kvmib+urz5I9ZF+0vuK2rVrs1s0" +
            "bJdkaUkFyIGnRtzRIM7LJnjrL+enXjv9fHeCa73Vb47ReiKE5Ae65qRIsoRMaM2rJlFal5kHnn2qMbG1E588oTd7dLEPGQAqJuWALikMevOynzE4N5l6638+3JzQ" +
            "WnflMV1pLneRQkDdrCRQQe6aPRqJe+VBUtGayjg0/mhrIms/X9lXXJ2tBCMiNELbtCxQVyW0MbsQ9UYHu/794DXZb/r+ff0peLfqL5bMQ2BmXplGaGuUjJu2/vZP" +
            "t+ovPvRi87b9bg91yRE5J4mnGphJ5q1u2S+kVQNtd5SOfr4vPz9d3UlGDKik8tIXOUfqqMir3t4bvcHBhX/uDdkVlVcHjmQ5AQ5lrmF2LzRkpM6/677NqwPHgy92" +
            "PlotnZuuRUlmKMin1aaAA1nedKt+XOt8id/zr/VBi3J6Vf+7gkuS6ZxcEw/ykhnZP6wvz1QH3+9/sfOvPTHd6qENkjUH1FPsckmmc5HlQ142Gw++3DprTMhjX7yo" +
            "dNPW37LBq0rf0XYjBmfysvntP/dfFWSqDTv7sDG5aeuvzxAu6fblweumrb8+O7nysOkt4/U6OxrOjL4zo4jOFdBLOShBuDTrm/fd5YdmHnq+UWpFQXe52SqNPFBI" +
            "O0QEPWlqfl41XgdfbJ2wES/3ql4CKSz5WBcUSL/1yoOk/S93w0WOGqj/CVt/Pa1itQe5B1VCe1Vz6T/3XrkpevWnRo9r7N7++yPJnOFDEoXMVTdt/e3hT3pfUXvt" +
            "9PPdkSMcqS/zwdWLlBbMvHZttuj7Dx7W2vN2vSNquNk6vFukEfZ+5OZgwqBTy77/5KvCPa8/1xLRZ4/uliJsPEkyIh0HxUzpiU+e0IsWaX//wcNas0ejfISNLxlf" +
            "sJ09GuXHuvP7/YdVB52bR+DkTeoanAadm0ff7z+suudgvSdK+N+/Uu/DhPEcvCMH9K/U+z9f2Vd81XhvIEKzu1KvnpLMGR4NJkjmjJV69ZQKmfANcb7cJCHW+jS0" +
            "g1ibLzdJh55vhoYGzUd9f2lyv1djVNLk/nzU9z/4ajMvLGZv1JtO5BG/BtuIzOyNetMPvNouDxrnaMTVkD3r9IJsjbh69mFjsu//W/UBa9+Yy5uV45zEw4eNubz5" +
            "2BcvKoXrq67MutlBos6kA7qbtv6eEwyw96S3juDt3ZA6cmvvSW/dgdfbF8LgnRl9x0KLyPFvboAWkXNm9B2L/a92q4MM0j1nc/AMNP0z0NQoF5e/fF4uxNDfszeP" +
            "IlSM93Cyhorxz948igiRO4YIH2edQMRmzrly8pgsJ6bkjh1rvchy4ujn+/JVRwHmp91dZTL5G9nh7Pt38dOf38SFo1X69ThJn6tW64qnnhvwmuu/Y9+tnzETN5RI" +
            "ns9Pu7sqsp5eediKFF0C5crpHN94837OHXZJ/X/er8f5258v4dHd9/L087d53+Nb7OycMZ2GFN1d6VlXG2CSuuaCSon1Tffg6XvusMvX33yAjyw/SzOumT4uQ11y" +
            "KnF2q96iMoN133v/Ts4ddvHhdXj+cJXvv38HB3o/ocRIWbfqLTr0YvtOhVyfZBwsw/KTn99MmheUUvI/XD1HCRTJOFihf7d75+8hYFGG5Z9/reGFnfurSxkUsNi9" +
            "8/eQ8sk+aWr4JkyZo0eOe3ETJL3v74pkuN96jlEKSJoa3l15TLfsRAVpLnchtAAAAAAAAAAnp2AitBr1aHdJnNPrfX1SCAB4u6gScnq9r394Y7OpFE3j1F5Q99dA" +
            "rXFqbymY79/9hgEyAAD8XZAB37/7DYM9O5+WlGAZnJsIABUXbXojPDg/FVgC5ehKT11SEAB4vEhTA4+u9NQXxttdU0nm69hsdQ6U/SlJ83Vsnh4naNgOAD4vsG1q" +
            "3DA3XfWUZOm1gdXcdNVzGgdZ/AHAz2XqCZuWmxE5y28TOXtKzkZ1NoGs3wYyRZQtEyHUvqyGTQDg94LYoPZlxd7IBIa86k4GU3cs7Bs/8qo7eRIDaQ0A+LtMPGmT" +
            "UpfCYt9NWDwhqfmcjcGUIMMTrg1gPHfjcOo4gYZF5hS9qHXN2+dHbC+SSlSrTfDgyRS/r8ewJrOIsnAcADHPnG16NartRfL2+RGfOpzDmkyE23jSD743Z9uLpDKW" +
            "OZnw3emVh60Oj9dFIw8wc3Y1DwAAwPe7pa5uozUVrY6b/0aYN1qrZE713yRz6kilz3522854XkNhBVagHwBaiR2wAwRszy5+9rPbdsPjyDLRtq/DeegDoAlgDS5D" +
            "C54GTTCrbsK+REywvX4e+jAK0Ifz8EwwuzphXzInmbYr8H8fW1ZgJ1iVN7LvcNIgGJu2VZiEsAKm5YHxiU+e0Lv1Px9uBuYOz21s37MWIDEJwbS+YUkRHftkLsqm" +
            "DxwpMT6Hcy6xBhMQWgnL+kZKkDa2de2AyzAJwbK+kRKktXFrwfiBLTBtw5KTa40OQsv4i/FpsNlwjfrTQMD4UNLJtUZnZhbm1q8qTXgmXIFV6AeAVmI77AQB6/pm" +
            "ZmEO5vYvj7ug+lXYygcASDIHAAAAAP+72pLMpakGdWHSVAOCoC6MIEDo1oUhdDXo1YVpEEqTQtOintbCVI7zNSxLJAi1CJW6+ENSRWrVhSGUEHJ1YajKxpI001+H" +
            "KZnJ20TfWO7wnBwAAAAAAAAAAAAAAAAAAAAAAIDa+ANd0BVvtdrIStVM/moGJg05jBw2KeahkynePj9iW5FUolppgodPpjApFaUP0WlSzO/qMT55OEeCG8h+abKn" +
            "LooJPVpES10YSSqpuS5MUjOytS4M2VpEaa2NDZGSujAtigRZUxemoUajVNeFQc1GO2vqwjbaWXPL5lZbXvbaEVq14MiOWza32gBEFTjVgiGqAABRWQfbIyXIyjqw" +
            "kRJCRT2YFuULAAy6U1/qwdbnJyoA4Nb/fLg5L5t6MA7y3ijvHP4fflIP6QVhpfe3w32H+xHx0vvtCfuKfOG9Tdj3r2srnzdTNf4nm/66tvJ5eNzidw8Od6v+A8n0" +
            "cN804snoBGjJLKzG9s3zZ7hokopGaypanUf+31O0ysPRmg6P10V52fwH1s47iA6P10VjAMhHiBDnDflwHEAjbnm/jc7tcYCDL24/0wiJZBq47hohOfDyf0/HCcTe" +
            "SF72jvi+KVmvT3o/E1mu+b4tTV6fhHDg1eErqKvwxiyNimTBiZNUtK75a/kI5Ymgbv/ro5ebDBM287IXj4goj9fvDpg7WsMJG3TuUIEIeU7sjUyEaNZxGkbLf3/V" +
            "1St/JLknbXj5MNqs4/Rkir1vtsTdsl9Y/msGVq7/hn73Dq3je3HB+907nHtwi/JEKor3vtkST8EgHJXMW6X5NG64cPcF+t0jWscreOAX7r6Axg3lkYiaNw1jrXd4" +
            "H6rBojTgRvuMcw9u8e/bP2bbb2+gdbyE5DhJl2hc0e8OuXrlD5x78F00KuWhNIjXeoMH0zgW/rk3lJdNMGRWOPjKjV8A/GX5UUIrXn22QwCy7asH2Dm5kg5P/9iq" +
            "G608LZntPn8QgWS232jl6ekkC+PtrrxqEklhLhuReQvj7a4SKLOHF9mDc5Mijz+oA3qbR1dZZVj27Hxakpe9g4gdDhvy6J6dT0tKwRSdktI4TTGouevQXXRKSjma" +
            "wxubTb3yIEmVUDO6ngzJGCn78MZmU0mcojWVIfVlNWg6ewud2p5IK8tzaPzR1rzq7SUj1VkjMrH893TdOJrNhBowd9Wh9sbRbGZ5ooXtD/YtTRaXK9eMKPtE75QA" +
            "ZJ/525cxoU5Y0Rm01ysgdQbt9fbBUqFkznH0Fko6g/Z6Jahibixo5vSqApTddOjb6IwHVKO6d+Hov/bB8n7JmuymaZTYexeO/quIdan/zoynTH76DuyddPh8qf/O" +
            "jKpcx3xvSAte0ngDci46DGvB65jvDVUmyxbbD0v7C7EaERVT1pPblbdeGvHSzjXzkQQC/NztFgAAbmTwm9MxHv5jikuzQjyBkpmYLbYfAqBdGL4n/imTnzxFjI+n" +
            "6intyseun7GlSIJSzkfy2vkrPn79jE//PMfFWSGWQOSrC8P3xA/ZKhetz5V1zbP8DBrR7C2XRmwpktCcW4rkLZdGfPmXGSLpg2/rc2XdMd8bCkJ3362huP3D0npp" +
            "8lw0e2nnmvCgL+tcE0ufqEH33RqKA+Fli8PzS/sLJzRiq3PHkE51BqfnggFeGXYDtx8MnktyjiTsN6djvHb+iuCgvz4dI446De+3ztrAcISP+1M/ylLJ+j7SJyY9" +
            "+sckH71+Tug7rjfBo39MEkUcNKMs/eo9fQEZs/5xXftgcbak/Dit14VZh0//PMdbLo142dwVcwUCUq41+PXZOI/8McXFWSFKi6VhbdY/rgtKmfXb4vZk2V/U3Dg3" +
            "F2eFL/8yQwI7qRKc9dviwJjZ4DhvaX8+WaPsd6oGyZqSDY7zgoN2hqdhi5PF87DWJYeLneFpWHnSCuMA+mu7Ra4u5sMMhxzKtp61XjAagTVbMfC+KbW12UIxYrw7" +
            "Dq+1M7Hwq/f0RaH9Rk93++2yFl3lktXBGdcon/SkzMsWBl2ReLMHxx3td3N6NGPlkvmLK64Rf1Bczc0eHHdEI85unzXfMV3Tb9YXxZJp68l6M67nfGfxtCUi8/29" +
            "w6Y7Ji8RNOO8QJKzQY8WIvQvFPcHrg7bo1LfP/iP9G0/rWjMnVzfk8xpTjiUa2diwQNXh+2RuR+6cfTtg9uUr7cXz2hY6YLDta1n7cav3tMXnfyr9/SBzNL+fJIH" +
            "PQpS6vKwDS3TXkVoiDvD09DF/UVvQm7q33EDQ1oIzBbb40b4l4ftb4sHi7NIl0GYtEMLYfVyvy0xo2C535a0D5ZNUW+m/IjwQaMsCRAghIyDPrhN+Xp7IVsjtqYb" +
            "PG+dtYFT6o9fPbA4WdqIzAKN2GuxMjIqQrLB8Iw5JcuD4dmlvUUrLVyAcSl2SKWxrrPUigxq6Sy1ots9IP/U/Z9EakQYyKd2fUQmXxy8J+6Y7w1FkBMkMdgZnkS/" +
            "c2/RuSicALukHL40Gl7fXWrfm1X03aX2/e0ekH/K5Kd7IBJU0plaAPEXB+9OHyu1Wbg8aJPv+rDy3czZVRZpbiKuEUWNrMGdwcm/BnRVHGAC2j8sLZYm01KY9Q1V" +
            "WsTebHF4OwFt2eLwdntMUa4tBZMZAdrGlUE7EQn6yzArWzEQ46kWI2EA3DF5iWA2z/cQAkHd8iQPUvYgZtPvH/xHGva1HzcaAu6YrunPzkb7wQ9UrF5WEMcG7cnk" +
            "+3uHTTGuxTEDVODODysGzZOrbQRf0LZYD+nY+vz4kfsWjiTJNfH3LRxJgPaHVfXG8cVmIrZrsDQmRZKYzMNFd/J0tjDoSrLPlS0MuoA2co29eU8tig/MBTk7I6aE" +
            "Aqnrb8XS6YOM4WT7wBnDnaXTu8A7D6ZNipzchPQK+10fFSaVa8TJGRenv9u/W594jPjd/t164MhTsr/4enmCZF0By8AgMjtIJPNmUXSuvvKb45dHnhpxIGY/8tQI" +
            "ABx5SvbXX81NKaKzZPP3WsVraiG/kTe3Xvbt2dOxEgcKhvrb+wvWFJ15ktUNpoJa+CYKnmiUfJqbgmx4UuVO7jobnlQBwIf+ReE/t+9xJm4mSRoPrmAUjBO+whsN" +
            "r8jO85ve/f/95z8z6NTYzsjZeNfb2TEb3TEbzbBtZLVRYUuwAq1q0wBJVSIriiiVElkxc3xd+Z0H543uzTgZPS93TF4iaBbn5lIz1wgzkQZECMgUgEAyBRqhBNoA" +
            "KIEqAD3QD0C7ZPZrhBSkREjJlKqQSGatttSsN7M1Rns7AA==",
        Base64.DEFAULT,
    )
val illustrationSample: OSItemIllustration.Image = OSItemIllustration.Image(OSImageSpec.Data(iconSample))
val drawableSample: OSImageSpec.Drawable = OSImageSpec.Drawable(R.drawable.os_ic_sample)
val randomColor: Color
    get() = Color(Random.nextInt(0, 100) / 100f, Random.nextInt(0, 100) / 100f, Random.nextInt(0, 100) / 100f)
