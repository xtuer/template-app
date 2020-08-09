<template>
    <div class="view-404">
        <div class="content-wrapper">
            <img src="/static-p/img/cry.jpg">
            <div class="block-content">
                <h1 style="text-align: center; color: black">AWWW...Don’t Cry.</h1>
                <p style="text-align: center;">
                    <br>It's just a 404 Error!<br>
                    What you’re looking for may have been misplaced in Long Term Memory.<br>
                </p>
            </div>
        </div>
    </div>
</template>

<style lang="scss">
.view-404 {
    .content-wrapper {
        display: grid;
        grid-template-columns: max-content 790px;
        justify-items: center;
        justify-content: center;
        padding-top: 100px;

        img {
            width: 300px;
        }

        .block-content {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            font-size: 22px;
        }
    }

}
</style>
