#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 v_texPo;
uniform samplerExternalOES sTexture;

void main() {
//原始效果
//    gl_FragColor=texture2D(sTexture, v_texPo);
//黑白效果
    lowp vec4 textureColor = texture2D(sTexture, v_texPo);
    float gray = textureColor.r * 0.299 + textureColor.g * 0.587 + textureColor.b * 0.114;
    gl_FragColor = vec4(gray, gray, gray, textureColor.w);
}