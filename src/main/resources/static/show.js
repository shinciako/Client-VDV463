if(performance.navigation.type == 2){
   location.reload(true);
}

$(document).ready(function () {
    $('input[type="radio"]').click(function(){
            if($(this).attr("value")=="BootNotification"){
                $(".box").hide("slow");
            }
            if($(this).attr("value")=="ProvideChargingRequests"){
                $(".box").show("fast");

            }
        });
    $('input[type="radio"]').trigger('click');
});

$(document).ready(function()
{
  $(".message:odd").css({
    "box-shadow": "inset 0 0 1em gold"});
});

function connectMessage(){
   if(!alert('Connecting to dev.eos-dev.ekoenergetyka.com.pl/api/se/vdv463/ws\nProtocol: v1.463.vdv.de\n\nPress OK to connect')){refreshPage();}
}

function exitMessage(){
    if(!alert('Press ok to disconnect')){refreshPage();}
}


function refreshPage(){
    window.location.reload();
}